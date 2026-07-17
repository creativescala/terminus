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
import terminus.Eof
import terminus.Key
import terminus.KeyParser
import terminus.Timeout

import scala.concurrent.duration.*

/** A Cats Effect driver for [[terminus.KeyParser]]: the concurrent counterpart
  * of the blocking [[terminus.effect.TerminalKeyReader]].
  */
object KeyReader:
  /** Read a single key press from `chars`. The timeout is how long we wait
    * between an escape being pressed and another key before we decide they are
    * separate key presses.
    *
    * Canceling `chars` must not lose a character, because the driver reads with
    * a timeout while disambiguating an escape. A queue take, as produced by
    * [[CharSource.pump]], has this property; a blocking read wrapped in
    * `IO.blocking` does not.
    */
  def readKey(
      chars: IO[Eof | Char],
      timeout: FiniteDuration = 100.millis
  ): IO[Eof | Key] =
    def loop(parser: KeyParser): IO[Eof | Key] =
      val input: IO[Timeout | Eof | Char] =
        if parser.isDisambiguating then
          chars.timeoutTo(timeout, IO.pure(Timeout))
        else chars

      input.flatMap { token =>
        parser.parse(token) match
          case Eof             => IO.pure(Eof)
          case key: Key        => IO.pure(key)
          case next: KeyParser => loop(next)
      }

    loop(KeyParser.Start)
