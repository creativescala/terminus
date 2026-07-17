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

package terminus.ui.ce

import cats.effect.IO
import cats.effect.std.Queue
import terminus.Eof
import terminus.Key
import terminus.ce.KeyReader
import terminus.effect.TerminalDimensions
import terminus.ui.runtime.Event

import scala.concurrent.duration.FiniteDuration

/** The producer and consumer sides of the event queue. Producers — the key
  * reader here, timers and resize notifications in future — offer [[Event]]s;
  * the single consumer feeds them to the step function returned by
  * `FullScreen.eventLoop`, which keeps the reactive graph effectively
  * single-threaded.
  */
private[ui] object Events:
  /** Read keys from `chars` and offer them to `queue` as input events,
    * finishing after offering `Eof`.
    */
  def keys(chars: IO[Eof | Char], queue: Queue[IO, Event]): IO[Unit] =
    KeyReader.readKey(chars).flatMap {
      case Eof      => queue.offer(Event.Input(Eof))
      case key: Key => queue.offer(Event.Input(key)) >> keys(chars, queue)
    }

  /** Poll `dimensions` every `interval` and, when the value changes, offer an
    * effect that runs `update` with the new value. Polling rather than SIGWINCH
    * is deliberate: a signal handler can only safely set a flag that something
    * must then poll anyway, and the char pump already polls at a similar
    * cadence. When core grows a resize-notification hook this producer's
    * implementation changes; its place in the design does not.
    */
  def resizes(
      dimensions: IO[TerminalDimensions],
      update: TerminalDimensions => Unit,
      queue: Queue[IO, Event],
      interval: FiniteDuration
  ): IO[Unit] =
    def loop(previous: TerminalDimensions): IO[Unit] =
      IO.sleep(interval) >> dimensions.flatMap { current =>
        if current == previous then loop(previous)
        else queue.offer(Event.Effect(() => update(current))) >> loop(current)
      }

    dimensions.flatMap(loop)

  /** Consume events one at a time, finishing when `step` returns false. */
  def consume(queue: Queue[IO, Event], step: Event => Boolean): IO[Unit] =
    queue.take.flatMap { event =>
      IO(step(event)).flatMap { continue =>
        if continue then consume(queue, step) else IO.unit
      }
    }
