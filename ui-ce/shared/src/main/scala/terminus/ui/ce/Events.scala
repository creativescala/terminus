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
import terminus.ui.runtime.Event

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

  /** Consume events one at a time, running `beforeStep` and then `step` for
    * each, finishing when `step` returns false.
    */
  def consume(
      queue: Queue[IO, Event],
      beforeStep: IO[Unit],
      step: Event => Boolean
  ): IO[Unit] =
    queue.take.flatMap { event =>
      beforeStep >> IO(step(event)).flatMap { continue =>
        if continue then consume(queue, beforeStep, step) else IO.unit
      }
    }
