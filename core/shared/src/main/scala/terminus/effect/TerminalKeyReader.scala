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
import terminus.Timeout

import scala.concurrent.duration.*

/** An implementation of KeyReader that interprets the standard terminal escape
  * codes for key presses. The timeout is how long we wait between an escape
  * being pressed and another key before we decide they are separate key
  * presses.
  */
trait TerminalKeyReader(timeout: Duration = 100.millis) extends KeyReader {
  self: NonBlockingReader & Reader =>
  def readKey(): Eof | Key =
    read() match {
      case ' '       => Key.Space
      case Ascii.NUL => Key.ControlAt // Control-At (Also for Ctrl-Space)
      case Ascii.SOH => Key.ControlA // Control-A (home)
      case Ascii.STX => Key.ControlB // Control-B (emacs cursor left)
      case Ascii.ETX => Key.ControlC // Control-C (interrupt)
      case Ascii.EOT => Key.ControlD // Control-D (exit)
      case Ascii.ENQ => Key.ControlE // Control-E (end)
      case Ascii.ACK => Key.ControlF // Control-F (cursor forward)
      case Ascii.BEL => Key.ControlG // Control-G
      case Ascii.BS  => Key.Backspace // Control-H (8) (Identical to '\b')
      case Ascii.HT  => Key.Tab // Control-I (9) (Identical to '\t')
      case Ascii.LF  => Key.ControlJ // Control-J (10) (Identical to '\n')
      case Ascii.VT =>
        Key.ControlK // Control-K (delete until end of line; vertical tab)
      case Ascii.FF  => Key.ControlL // Control-L (clear; form feed)
      case Ascii.CR  => Key.Enter
      case Ascii.SO  => Key.ControlN // Control-N (14) (history forward)
      case Ascii.SI  => Key.ControlO // Control-O (15)
      case Ascii.DLE => Key.ControlP // Control-P (16) (history back)
      case Ascii.DC1 => Key.ControlQ // Control-Q
      case Ascii.DC2 => Key.ControlR // Control-R (18) (reverse search)
      case Ascii.DC3 => Key.ControlS // Control-S (19) (forward search)
      case Ascii.DC4 => Key.ControlT // Control-T
      case Ascii.NAK => Key.ControlU // Control-U
      case Ascii.SYN => Key.ControlV // Control-V
      case Ascii.ETB => Key.ControlW // Control-W
      case Ascii.CAN => Key.ControlX // Control-X
      case Ascii.EM  => Key.ControlY // Control-Y (25)
      case Ascii.SUB => Key.ControlZ // Control-Z

      case '\u009b' => Key.ShiftEscape
      case '\u001c' => Key.ControlBackslash
      case '\u001d' => Key.ControlSquareClose
      case '\u001e' => Key.ControlCircumflex
      case '\u001f' => Key.ControlUnderscore

      case Ascii.ESC =>
        read(timeout) match {
          case Eof     => Key.Escape
          case Timeout => Key.Escape
          // Normal mode
          case '[' =>
            read() match {
              case Eof         => Eof
              case 'A'         => Key.Up
              case 'B'         => Key.Down
              case 'C'         => Key.Right
              case 'D'         => Key.Left
              case 'F'         => Key.End
              case 'H'         => Key.Home
              case other: Char => Key.Character(other)
            }
          // Application mode
          case 'O' =>
            read() match {
              case Eof         => Eof
              case 'A'         => Key.Up
              case 'B'         => Key.Down
              case 'C'         => Key.Right
              case 'D'         => Key.Left
              case 'F'         => Key.End
              case 'H'         => Key.Home
              case other: Char => Key.Character(other)
            }
          case other: Char => Key.Character(other)
        }

      case Eof         => Eof
      case other: Char => Key.Character(other)
    }
}
