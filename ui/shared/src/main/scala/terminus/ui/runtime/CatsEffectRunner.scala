/*
 * Copyright 2024 Creative Scala
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package terminus.ui.runtime

import cats.effect.IO
import cats.effect.std.Dispatcher
import cats.effect.std.Queue
import cats.effect.std.Supervisor
import fs2.Stream
import terminus.Eof
import terminus.Key
import terminus.ce.CharSource
import terminus.ce.KeyReader
import terminus.effect

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import scala.concurrent.duration.*

/** A [[Runner]] that uses Cats Effect.
  *
  * This works at two stages: setup time, when effects can be registered but
  * don't run, and run time, when effects can be registered and run. inQueue
  * transfers effects from setup time to run time.
  *
  * outQueue holds effects that must run on the Runtime thread, not within Cats
  * Effects. This is anything that affects the Runtime.
  */
final class CatsEffectRunner(
    dispatcher: Dispatcher[IO],
    inQueue: Queue[IO, Stream[IO, Runnable]],
    supervisor: Supervisor[IO]
) extends Runner:
  def schedule(io: IO[Runnable]): Unit =
    dispatcher.unsafeRunAndForget(inQueue.offer(Stream.eval(io)))

  def schedule(stream: Stream[IO, Runnable]): Unit =
    dispatcher.unsafeRunAndForget(inQueue.offer(stream))

  def run(
      terminal: effect.Dimensions & effect.NonBlockingReader
  ): BlockingQueue[Event] =
    val resizePollInterval = 100.millis
    val outQueue = ArrayBlockingQueue[Event](128)
    // The supervisor owns the IO effects fibers, canceling any still running
    // when the session ends.
    val effects = Stream
      .fromQueueUnterminated(inQueue)
      .evalMap(s =>
        supervisor.supervise(
          s.evalMap(r => IO.blocking(outQueue.put(Event.Effect(r))))
            .compile
            .drain
        )
      )
      .compile
      .drain

    val resize = Stream
      .awakeDelay[IO](resizePollInterval)
      .evalMap(_ => IO.blocking(terminal.getDimensions))
      .filterWithPrevious((p, c) => p != c)
      .evalMap(d => IO.blocking(outQueue.put(Event.Resize(d))))
      .compile
      .drain

    val keys = CharSource.fromReader(terminal).use { chars =>
      def loop(): IO[Unit] =
        KeyReader.readKey(chars).flatMap {
          case Eof      => IO.blocking(outQueue.put(Event.Input(Eof)))
          case key: Key =>
            IO.blocking(
              outQueue.put(Event.Input(key))
            ) >> loop()
        }

      loop()
    }

    val ceEffect =
      supervisor.supervise(
        effects.background.surround {
          resize.background.surround {
            // Keys is the one that will signal stop, so it's the inner most
            // effect
            keys
          }
        }
      )

    dispatcher.unsafeRunAndForget(ceEffect)
    outQueue
object CatsEffectRunner:
  def apply: IO[CatsEffectRunner] =
    val dispatcherResource = Dispatcher.parallel[IO]
    val queue = Queue.bounded[IO, Stream[IO, Runnable]](128)

    dispatcherResource.use { dispatcher =>
      Supervisor[IO].use { supervisor =>
        queue.map { new CatsEffectRunner(dispatcher, _, supervisor) }
      }
    }
