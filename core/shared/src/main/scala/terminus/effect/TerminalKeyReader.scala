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
      case ' '       => Key.space
      case Ascii.NUL => Key.controlAt // Control-At (Also for Ctrl-Space)
      case Ascii.SOH => Key.controlA // Control-A (home)
      case Ascii.STX => Key.controlB // Control-B (emacs cursor left)
      case Ascii.ETX => Key.controlC // Control-C (interrupt)
      case Ascii.EOT => Key.controlD // Control-D (exit)
      case Ascii.ENQ => Key.controlE // Control-E (end)
      case Ascii.ACK => Key.controlF // Control-F (cursor forward)
      case Ascii.BEL => Key.controlG // Control-G
      case Ascii.BS  => Key.backspace // Control-H (8) (Identical to '\b')
      case Ascii.HT  => Key.tab // Control-I (9) (Identical to '\t')
      case Ascii.LF  => Key.newLine // Control-J (10) (Identical to '\n')
      case Ascii.VT =>
        Key.controlK // Control-K (delete until end of line; vertical tab)
      case Ascii.FF  => Key.controlL // Control-L (clear; form feed)
      case Ascii.CR  => Key.enter
      case Ascii.SO  => Key.controlN // Control-N (14) (history forward)
      case Ascii.SI  => Key.controlO // Control-O (15)
      case Ascii.DLE => Key.controlP // Control-P (16) (history back)
      case Ascii.DC1 => Key.controlQ // Control-Q
      case Ascii.DC2 => Key.controlR // Control-R (18) (reverse search)
      case Ascii.DC3 => Key.controlS // Control-S (19) (forward search)
      case Ascii.DC4 => Key.controlT // Control-T
      case Ascii.NAK => Key.controlU // Control-U
      case Ascii.SYN => Key.controlV // Control-V
      case Ascii.ETB => Key.controlW // Control-W
      case Ascii.CAN => Key.controlX // Control-X
      case Ascii.EM  => Key.controlY // Control-Y (25)
      case Ascii.SUB => Key.controlZ // Control-Z

      case '\u009b' => Key.shiftEscape
      case '\u001c' => Key.controlBackslash
      case '\u001d' => Key.controlSquareClose
      case '\u001e' => Key.controlCircumflex
      case '\u001f' => Key.controlUnderscore
      case '\u007f' => Key.backspace
      case Ascii.ESC =>
        read(timeout) match {
          case Eof     => Key.escape
          case Timeout => Key.escape
          // Normal mode
          case '[' =>
            read() match {
              case Eof => Eof
              case 'A' => Key.up
              case 'B' => Key.down
              case 'C' => Key.right
              case 'D' => Key.left
              case 'F' => Key.`end`
              case 'H' => Key.home
              case digit: Char if digit.isDigit =>
                read() match {
                  case Eof => Eof
                  case '~' =>
                    digit match {
                      case '1'         => Key.home // tmux
                      case '2'         => Key.insert
                      case '3'         => Key.delete
                      case '4'         => Key.`end` // tmux
                      case '5'         => Key.pageUp
                      case '6'         => Key.pageDown
                      case '7'         => Key.home // xrvt
                      case '8'         => Key.`end` // xrvt
                      case other: Char => Key.unknown(s"${Ascii.ESC}[~${other}")
                    }
                  case other: Char => Key.unknown(s"${Ascii.ESC}[${other}")
                }
              case 'Z'         => Key.backTab
              case other: Char => Key.unknown(s"${Ascii.ESC}[${other}")
            }
          // Application mode
          case 'O' =>
            read() match {
              case Eof         => Eof
              case 'A'         => Key.up
              case 'B'         => Key.down
              case 'C'         => Key.right
              case 'D'         => Key.left
              case 'F'         => Key.`end`
              case 'H'         => Key.home
              case other: Char => Key.unknown(s"${Ascii.ESC}O${other}")
            }
          case other: Char => Key.unknown(s"${Ascii.ESC}${other}")
        }

      case Eof         => Eof
      case other: Char => Key(other)
    }
}
