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

package terminus.effect

import terminus.Eof
import terminus.Timeout

import scala.concurrent.duration.Duration

/** A Reader that reads input from the given String. Mostly useful for testing.
  */
class StringBufferReader(input: String)
    extends Reader,
      NonBlockingReader,
      KeyReader,
      TerminalKeyReader {
  private var index: Int = 0

  def read(): Eof | Char =
    if index >= input.size then Eof
    else {
      val char = input(index)
      index = index + 1
      char
    }

  def read(duration: Duration): Timeout | Eof | Char =
    read()
}
