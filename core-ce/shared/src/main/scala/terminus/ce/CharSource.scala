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

package terminus.ce

import cats.effect.IO
import cats.effect.Resource
import cats.effect.std.Queue
import terminus.Eof
import terminus.Timeout

import scala.concurrent.duration.*

object CharSource:
  /** Pump characters from `read` into a queue, yielding a take action whose
    * cancelation is safe: canceling a take — as [[KeyReader.readKey]] does with
    * a timeout while disambiguating an escape — never loses a character,
    * whereas canceling a blocking read abandons a read that will swallow the
    * next character.
    *
    * `read` is typically a timed blocking read such as core's
    * `NonBlockingReader.read(duration)` wrapped in `IO.blocking`; see
    * [[fromReader]]. A `Timeout` from `read` just reads again: its duration
    * bounds how long releasing the resource waits for the pump to notice
    * cancelation, not input latency, since a timed read returns as soon as a
    * character is available. The pump stops after delivering `Eof`.
    */
  def pump(read: IO[Timeout | Eof | Char]): Resource[IO, IO[Eof | Char]] =
    def loop(queue: Queue[IO, Eof | Char]): IO[Unit] =
      read.flatMap {
        case Timeout => loop(queue)
        case Eof     => queue.offer(Eof)
        case c: Char => queue.offer(c) >> loop(queue)
      }

    for
      queue <- Resource.eval(Queue.unbounded[IO, Eof | Char])
      _ <- loop(queue).background
    yield queue.take

  /** Pump characters from a core `NonBlockingReader`, polling with
    * `pollInterval`. See [[pump]] for the semantics; the poll interval only
    * bounds shutdown latency.
    */
  def fromReader(
      reader: terminus.effect.NonBlockingReader,
      pollInterval: FiniteDuration = 50.millis
  ): Resource[IO, IO[Eof | Char]] =
    pump(IO.blocking(reader.read(pollInterval)))
