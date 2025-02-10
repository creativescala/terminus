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
import terminus.KeyCode
import terminus.Timeout

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
    read() match {
      case ' '       => Key(' ')
      case Ascii.NUL => Key.control('@') // Control-At (Also for Ctrl-Space)
      case Ascii.SOH => Key.control('a') // Control-A (home)
      case Ascii.STX => Key.control('b') // Control-B (emacs cursor left)
      case Ascii.ETX => Key.control('c') // Control-C (interrupt)
      case Ascii.EOT => Key.control('d') // Control-D (exit)
      case Ascii.ENQ => Key.control('e') // Control-E (end)
      case Ascii.ACK => Key.control('f') // Control-F (cursor forward)
      case Ascii.BEL => Key.control('g') // Control-G
      case Ascii.BS =>
        Key(KeyCode.Backspace) // Control-H (8) (Identical to '\b')
      case Ascii.HT => Key('\t') // Control-I (9) (Identical to '\t')
      case Ascii.LF => Key('\n') // Control-J (10) (Identical to '\n')
      case Ascii.VT =>
        Key.control('k') // Control-K (delete until end of line; vertical tab)
      case Ascii.FF  => Key.control('l') // Control-L (clear; form feed)
      case Ascii.CR  => Key(KeyCode.Enter)
      case Ascii.SO  => Key.control('n') // Control-N (14) (history forward)
      case Ascii.SI  => Key.control('o') // Control-O (15)
      case Ascii.DLE => Key.control('p') // Control-P (16) (history back)
      case Ascii.DC1 => Key.control('q') // Control-Q
      case Ascii.DC2 => Key.control('r') // Control-R (18) (reverse search)
      case Ascii.DC3 => Key.control('s') // Control-S (19) (forward search)
      case Ascii.DC4 => Key.control('t') // Control-T
      case Ascii.NAK => Key.control('u') // Control-U
      case Ascii.SYN => Key.control('v') // Control-V
      case Ascii.ETB => Key.control('w') // Control-W
      case Ascii.CAN => Key.control('x') // Control-X
      case Ascii.EM  => Key.control('y') // Control-Y (25)
      case Ascii.SUB => Key.control('z') // Control-Z

      case '\u009b' => Key.shift(KeyCode.Escape)
      case '\u001c' => Key.control('\\')
      case '\u001d' => Key.control(']')
      case '\u001e' => Key.control('^')
      case '\u001f' => Key.control('_')

      case Ascii.ESC =>
        read(timeout) match {
          case Eof     => Key(KeyCode.Escape)
          case Timeout => Key(KeyCode.Escape)
          // Normal mode
          case '[' =>
            read() match {
              case Eof => Eof
              case 'A' => Key(KeyCode.Up)
              case 'B' => Key(KeyCode.Down)
              case 'C' => Key(KeyCode.Right)
              case 'D' => Key(KeyCode.Left)
              case 'F' => Key(KeyCode.End)
              case 'H' => Key(KeyCode.Home)
              case other: Char =>
                Key(other)
            }
          // Application mode
          case 'O' =>
            read() match {
              case Eof => Eof
              case 'A' => Key(KeyCode.Up)
              case 'B' => Key(KeyCode.Down)
              case 'C' => Key(KeyCode.Right)
              case 'D' => Key(KeyCode.Left)
              case 'F' => Key(KeyCode.End)
              case 'H' => Key(KeyCode.Home)
              case other: Char =>
                Key(other)
            }
          case other: Char =>
            Key(other)
        }

      case Eof         => Eof
      case other: Char => Key(other)
    }
}
