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

import scala.annotation.tailrec
import terminus.{Eof, Key, KeyMappings, KeySequence, Timeout}
import scala.concurrent.duration.*

/** An implementation of KeyReader that interprets the standard terminal escape
  * codes for key presses. The timeout is how long we wait between an escape
  * being pressed and another key before we decide they are separate key
  * presses.
  */
trait TerminalKeyReader(timeout: Duration = 100.millis) extends KeyReader {
  self: NonBlockingReader & Reader =>

  // Some references on parsing terminal codes:
  //   https://github.com/crossterm-rs/crossterm/blob/master/src/event/sys/unix/parse.rs
  //   https://github.com/Textualize/textual/blob/main/src/textual/_ansi_sequences.py
  def readKey(): Eof | Key =
    val input = read()

    input match
      case Eof => Eof
      case c: Char =>
        KeyMappings.default.get(c) match
          case None         => Key(c)
          case Some(k: Key) => k
          case Some(ks: KeySequence) =>
            read(timeout) match {
              case Eof     => ks.root
              case Timeout => ks.root
              case cx      => readKeySequence(s"$c$cx", ks)
            }

  @tailrec
  private def readKeySequence(acc: String, sequence: KeySequence): Eof | Key = {
    (sequence.sequences.get(acc), sequence.subSequences.contains(acc)) match
      case (Some(key), _) => key
      case (None, false)  => Key.unknown(acc)
      case (None, true) =>
        read() match {
          case Eof     => Eof
          case c: Char => readKeySequence(acc :+ c, sequence)
        }
  }
}
