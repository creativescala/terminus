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
import fs2.Stream
import terminus.effect

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import scala.collection.mutable

/** A [[Runner]] for tests: schedules are captured rather than run concurrently,
  * so a test can drive them deterministically instead of racing real fibers.
  * `IO[Runnable]`s are queued for [[drain]]; `Stream[IO, Runnable]`s (used for
  * repeating schedules like [[terminus.ui.timer.DefaultTimer.every]]) are
  * captured as-is so a test can pull exactly as many elements as it needs from
  * a stream that may never terminate on its own.
  */
private[ui] final class TestRunner extends Runner:
  private val pendingIOs: mutable.Queue[IO[Runnable]] = mutable.Queue.empty
  private val pendingStreams: mutable.Queue[Stream[IO, Runnable]] =
    mutable.Queue.empty

  def schedule(io: IO[Runnable]): Unit =
    pendingIOs.enqueue(io)

  def schedule(stream: Stream[IO, Runnable]): Unit =
    pendingStreams.enqueue(stream)

  def run(
      terminal: effect.Dimensions & effect.NonBlockingReader
  ): BlockingQueue[Event] =
    new ArrayBlockingQueue[Event](1)

  /** Run every `IO[Runnable]` scheduled so far, oldest first, in registration
    * order. Streams scheduled via the two-argument `schedule` are unaffected:
    * pull from [[streams]] directly to drive one.
    */
  def drain(): Unit =
    import cats.effect.unsafe.implicits.global
    while pendingIOs.nonEmpty do pendingIOs.dequeue().unsafeRunSync().run()

  /** The streams scheduled so far, oldest first, removed from the pending list.
    * A test drives one directly, e.g. `.take(2).compile.toList` to simulate two
    * ticks.
    */
  def streams: List[Stream[IO, Runnable]] =
    val result = pendingStreams.toList
    pendingStreams.clear()
    result
