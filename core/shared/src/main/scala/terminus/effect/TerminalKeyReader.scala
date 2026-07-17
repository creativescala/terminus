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
import terminus.Key
import terminus.KeyParser

import scala.annotation.tailrec
import scala.concurrent.duration.*

/** An implementation of KeyReader that interprets the standard terminal escape
  * codes for key presses, using [[terminus.KeyParser]] to do the parsing. The
  * timeout is how long we wait between an escape being pressed and another key
  * before we decide they are separate key presses.
  */
trait TerminalKeyReader(timeout: Duration = 100.millis) extends KeyReader:
  self: NonBlockingReader & Reader =>

  def readKey(): Eof | Key =
    @tailrec
    def loop(parser: KeyParser): Eof | Key =
      val input = if parser.isDisambiguating then read(timeout) else read()
      parser.parse(input) match
        case Eof             => Eof
        case key: Key        => key
        case next: KeyParser => loop(next)

    loop(KeyParser.Start)
