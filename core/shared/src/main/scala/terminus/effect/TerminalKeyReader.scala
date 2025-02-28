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

  /** Read a character. If it matches char return key otherwise construct an
    * unknown key using prefix as the path to reach this point.
    */
  private inline def readMatchOrUnknown(
      char: Char,
      key: Key,
      prefix: String
  ): Eof | Key =
    read() match {
      case Eof              => Eof
      case ch if ch == char => key
      case other: Char      => Key.unknown(prefix :+ other)
    }

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
          case Eof      => Key.escape
          case Timeout  => Key.escape
          case Ascii.HT => Key.backTab
          case 'b'      => Key.controlLeft
          case 'f'      => Key.controlRight
          // Normal mode
          case '[' =>
            read() match {
              case Eof => Eof
              case 'a' => Key.shiftUp
              case 'b' => Key.shiftDown
              case 'c' => Key.shiftRight
              case 'd' => Key.shiftLeft
              case 'A' => Key.up
              case 'B' => Key.down
              case 'C' => Key.right
              case 'D' => Key.left
              case 'F' => Key.`end`
              case 'H' => Key.home
              case 'Z' => Key.backTab
              case '1' =>
                read() match {
                  case Eof => Eof
                  case '1' =>
                    read() match {
                      case Eof   => Eof
                      case '^'   => Key.controlF1
                      case '~'   => Key.f1
                      case other => Key.unknown(s"${Ascii.ESC}[11${other}")
                    }
                  case '2' =>
                    read() match {
                      case Eof   => Eof
                      case '^'   => Key.controlF2
                      case '~'   => Key.f2
                      case other => Key.unknown(s"${Ascii.ESC}[12${other}")
                    }
                  case '3' =>
                    read() match {
                      case Eof   => Eof
                      case '^'   => Key.controlF3
                      case '~'   => Key.f3
                      case other => Key.unknown(s"${Ascii.ESC}[13${other}")
                    }
                  case '4' =>
                    read() match {
                      case Eof   => Eof
                      case '^'   => Key.controlF4
                      case '~'   => Key.f4
                      case other => Key.unknown(s"${Ascii.ESC}[14${other}")
                    }
                  case '5' =>
                    read() match {
                      case Eof => Eof
                      case '^' => Key.controlF5
                      case ';' =>
                        read() match {
                          case Eof => Eof
                          case '2' =>
                            read() match {
                              case Eof => Eof
                              case '~' => Key.f17
                              case other =>
                                Key.unknown(s"${Ascii.ESC}[15;2${other}")
                            }
                          case '5' =>
                            read() match {
                              case Eof => Eof
                              case '~' => Key.controlF5
                              case other =>
                                Key.unknown(s"${Ascii.ESC}[15;5${other}")
                            }
                          case '6' =>
                            read() match {
                              case Eof => Eof
                              case '~' => Key.controlF17
                              case other =>
                                Key.unknown(s"${Ascii.ESC}[15;6${other}")
                            }
                          case other => Key.unknown(s"${Ascii.ESC}[15;${other}")
                        }
                      case '~'   => Key.f5
                      case other => Key.unknown(s"${Ascii.ESC}[15${other}")
                    }
                  case '7' =>
                    read() match {
                      case Eof => Eof
                      case '^' => Key.controlF6
                      case ';' =>
                        read() match {
                          case Eof => Eof
                          case '2' =>
                            read() match {
                              case Eof => Eof
                              case '~' => Key.f18
                              case other =>
                                Key.unknown(s"${Ascii.ESC}[17;2${other}")
                            }
                          case '5' =>
                            read() match {
                              case Eof => Eof
                              case '~' => Key.controlF6
                              case other =>
                                Key.unknown(s"${Ascii.ESC}[17;5${other}")
                            }
                          case '6' =>
                            read() match {
                              case Eof => Eof
                              case '~' => Key.controlF18
                              case other =>
                                Key.unknown(s"${Ascii.ESC}[17;6${other}")
                            }
                          case other => Key.unknown(s"${Ascii.ESC}[17;${other}")
                        }
                      case '~'   => Key.f6
                      case other => Key.unknown(s"${Ascii.ESC}[17${other}")
                    }
                  case '8' =>
                    read() match {
                      case Eof => Eof
                      case '^' => Key.controlF7
                      case ';' =>
                        read() match {
                          case Eof => Eof
                          case '2' =>
                            read() match {
                              case Eof => Eof
                              case '~' => Key.f19
                              case other =>
                                Key.unknown(s"${Ascii.ESC}[18;2${other}")
                            }
                          case '5' =>
                            read() match {
                              case Eof => Eof
                              case '~' => Key.controlF7
                              case other =>
                                Key.unknown(s"${Ascii.ESC}[18;5${other}")
                            }
                          case '6' =>
                            read() match {
                              case Eof => Eof
                              case '~' => Key.controlF19
                              case other =>
                                Key.unknown(s"${Ascii.ESC}[18;6${other}")
                            }
                          case other => Key.unknown(s"${Ascii.ESC}[18;${other}")
                        }
                      case '~'   => Key.f7
                      case other => Key.unknown(s"${Ascii.ESC}[18${other}")
                    }
                  case '9' =>
                    read() match {
                      case Eof => Eof
                      case '^' => Key.controlF8
                      case ';' =>
                        read() match {
                          case Eof => Eof
                          case '2' =>
                            read() match {
                              case Eof => Eof
                              case '~' => Key.f20
                              case other =>
                                Key.unknown(s"${Ascii.ESC}[19;2${other}")
                            }
                          case '5' =>
                            read() match {
                              case Eof => Eof
                              case '~' => Key.controlF8
                              case other =>
                                Key.unknown(s"${Ascii.ESC}[19;5${other}")
                            }
                          case '6' =>
                            read() match {
                              case Eof => Eof
                              case '~' => Key.controlF20
                              case other =>
                                Key.unknown(s"${Ascii.ESC}[19;6${other}")
                            }
                          case other => Key.unknown(s"${Ascii.ESC}[19;${other}")
                        }
                      case '~'   => Key.f8
                      case other => Key.unknown(s"${Ascii.ESC}[19${other}")
                    }
                  case ';' =>
                    read() match {
                      case Eof => Eof
                      case '2' =>
                        read() match {
                          case Eof   => Eof
                          case 'A'   => Key.shiftUp
                          case 'B'   => Key.shiftDown
                          case 'C'   => Key.shiftRight
                          case 'D'   => Key.shiftLeft
                          case 'F'   => Key.shiftEnd
                          case 'H'   => Key.shiftHome
                          case 'P'   => Key.f13
                          case 'Q'   => Key.f14
                          case 'R'   => Key.f15
                          case 'S'   => Key.f16
                          case other => Key.unknown(s"${Ascii.ESC}[1;2${other}")
                        }
                      case '5' =>
                        read() match {
                          case Eof   => Eof
                          case 'A'   => Key.controlUp
                          case 'B'   => Key.controlDown
                          case 'C'   => Key.controlRight
                          case 'D'   => Key.controlLeft
                          case 'F'   => Key.controlEnd
                          case 'H'   => Key.controlHome
                          case 'P'   => Key.controlF1
                          case 'Q'   => Key.controlF2
                          case 'R'   => Key.controlF3
                          case 'S'   => Key.controlF4
                          case other => Key.unknown(s"${Ascii.ESC}[1;5${other}")
                        }
                      case '6' =>
                        read() match {
                          case Eof   => Eof
                          case 'A'   => Key.controlShiftUp
                          case 'B'   => Key.controlShiftDown
                          case 'C'   => Key.controlShiftRight
                          case 'D'   => Key.controlShiftLeft
                          case 'F'   => Key.controlShiftEnd
                          case 'H'   => Key.controlShiftHome
                          case 'P'   => Key.controlF13
                          case 'Q'   => Key.controlF14
                          case 'R'   => Key.controlF15
                          case 'S'   => Key.controlF16
                          case other => Key.unknown(s"${Ascii.ESC}[1;6{other}")
                        }
                      case other => Key.unknown(s"${Ascii.ESC}[1;${other}")
                    }
                  case '~'   => Key.home
                  case other => Key.unknown(s"${Ascii.ESC}[1${other}")
                }

              case '2' =>
                read() match {
                  case Eof => Eof
                  case '0' =>
                    read() match {
                      case Eof => Eof
                      case '^' => Key.controlF9
                      case ';' =>
                        read() match {
                          case Eof => Eof
                          case '2' =>
                            read() match {
                              case Eof => Eof
                              case '~' => Key.f21
                              case other =>
                                Key.unknown(s"${Ascii.ESC}[20;2${other}")
                            }
                          case '5' =>
                            read() match {
                              case Eof => Eof
                              case '~' => Key.controlF9
                              case other =>
                                Key.unknown(s"${Ascii.ESC}[20;5${other}")
                            }
                          case '6' =>
                            read() match {
                              case Eof => Eof
                              case '~' => Key.controlF21
                              case other =>
                                Key.unknown(s"${Ascii.ESC}[20;6${other}")
                            }
                          case other => Key.unknown(s"${Ascii.ESC}[20;${other}")
                        }
                      case '~'   => Key.f9
                      case other => Key.unknown(s"${Ascii.ESC}[20${other}")
                    }
                  case '1' =>
                    read() match {
                      case Eof => Eof
                      case '^' => Key.controlF10
                      case ';' =>
                        read() match {
                          case Eof => Eof
                          case '2' =>
                            read() match {
                              case Eof => Eof
                              case '~' => Key.f22
                              case other =>
                                Key.unknown(s"${Ascii.ESC}[21;2${other}")
                            }
                          case '5' =>
                            read() match {
                              case Eof => Eof
                              case '~' => Key.controlF10
                              case other =>
                                Key.unknown(s"${Ascii.ESC}[21;5${other}")
                            }
                          case '6' =>
                            read() match {
                              case Eof => Eof
                              case '~' => Key.controlF22
                              case other =>
                                Key.unknown(s"${Ascii.ESC}[21;6${other}")
                            }
                          case other => Key.unknown(s"${Ascii.ESC}[21;${other}")
                        }
                      case '~'   => Key.f10
                      case other => Key.unknown(s"${Ascii.ESC}[21${other}")
                    }
                  case '3' =>
                    read() match {
                      case Eof => Eof
                      case '@' => Key.controlF21
                      case '^' => Key.controlF11
                      case '$' => Key.f23
                      case ';' =>
                        read() match {
                          case Eof => Eof
                          case '2' =>
                            read() match {
                              case Eof => Eof
                              case '~' => Key.f23
                              case other =>
                                Key.unknown(s"${Ascii.ESC}[23;2${other}")
                            }
                          case '5' =>
                            read() match {
                              case Eof => Eof
                              case '~' => Key.controlF11
                              case other =>
                                Key.unknown(s"${Ascii.ESC}[23;5${other}")
                            }
                          case '6' =>
                            read() match {
                              case Eof => Eof
                              case '~' => Key.controlF23
                              case other =>
                                Key.unknown(s"${Ascii.ESC}[23;6${other}")
                            }
                          case other => Key.unknown(s"${Ascii.ESC}[23;${other}")
                        }
                      case '~'   => Key.f11
                      case other => Key.unknown(s"${Ascii.ESC}[23${other}")
                    }
                  case '4' =>
                    read() match {
                      case Eof => Eof
                      case '@' => Key.controlF22
                      case '^' => Key.controlF12
                      case '$' => Key.f24
                      case ';' =>
                        read() match {
                          case Eof => Eof
                          case '2' =>
                            read() match {
                              case Eof => Eof
                              case '~' => Key.f24
                              case other =>
                                Key.unknown(s"${Ascii.ESC}[24;2${other}")
                            }
                          case '5' =>
                            read() match {
                              case Eof => Eof
                              case '~' => Key.controlF12
                              case other =>
                                Key.unknown(s"${Ascii.ESC}[24;5${other}")
                            }
                          case '6' =>
                            read() match {
                              case Eof => Eof
                              case '~' => Key.controlF24
                              case other =>
                                Key.unknown(s"${Ascii.ESC}[24;6${other}")
                            }
                          case other => Key.unknown(s"${Ascii.ESC}[24;${other}")
                        }
                      case '~'   => Key.f12
                      case other => Key.unknown(s"${Ascii.ESC}[24${other}")
                    }
                  case '5' =>
                    read() match {
                      case Eof   => Eof
                      case '^'   => Key.controlF13
                      case '~'   => Key.f13
                      case other => Key.unknown(s"${Ascii.ESC}[25${other}")
                    }
                  case '6' =>
                    read() match {
                      case Eof   => Eof
                      case '^'   => Key.controlF14
                      case '~'   => Key.f14
                      case other => Key.unknown(s"${Ascii.ESC}[26${other}")
                    }
                  case '8' =>
                    read() match {
                      case Eof   => Eof
                      case '^'   => Key.controlF15
                      case '~'   => Key.f15
                      case other => Key.unknown(s"${Ascii.ESC}[28${other}")
                    }
                  case '9' =>
                    read() match {
                      case Eof   => Eof
                      case '^'   => Key.controlF16
                      case '~'   => Key.f16
                      case other => Key.unknown(s"${Ascii.ESC}[29${other}")
                    }
                  case '~'   => Key.insert
                  case other => Key.unknown(s"${Ascii.ESC}[2${other}")
                }

              case '3' =>
                read() match {
                  case Eof => Eof
                  case '1' =>
                    read() match {
                      case Eof   => Eof
                      case '^'   => Key.controlF17
                      case '~'   => Key.f17
                      case other => Key.unknown(s"${Ascii.ESC}[31${other}")
                    }
                  case '2' =>
                    read() match {
                      case Eof   => Eof
                      case '^'   => Key.controlF18
                      case '~'   => Key.f18
                      case other => Key.unknown(s"${Ascii.ESC}[32${other}")
                    }
                  case '3' =>
                    read() match {
                      case Eof   => Eof
                      case '^'   => Key.controlF19
                      case '~'   => Key.f19
                      case other => Key.unknown(s"${Ascii.ESC}[33${other}")
                    }
                  case '4' =>
                    read() match {
                      case Eof   => Eof
                      case '^'   => Key.controlF20
                      case '~'   => Key.f20
                      case other => Key.unknown(s"${Ascii.ESC}[34${other}")
                    }
                  case '^' => Key.controlDelete
                  case '$' => Key.shiftDelete
                  case ';' =>
                    read() match {
                      case Eof => Eof
                      case '2' =>
                        readMatchOrUnknown(
                          '~',
                          Key.shiftDelete,
                          s"${Ascii.ESC}[3;2"
                        )
                      case '5' =>
                        readMatchOrUnknown(
                          '~',
                          Key.controlDelete,
                          s"${Ascii.ESC}[3;5"
                        )
                      case '6' =>
                        readMatchOrUnknown(
                          '~',
                          Key.controlShiftDelete,
                          s"${Ascii.ESC}[3;6"
                        )
                      case other => Key.unknown(s"${Ascii.ESC}[3;${other}")
                    }
                  case '~'   => Key.delete
                  case other => Key.unknown(s"${Ascii.ESC}[3${other}")
                }
              case '4' =>
                read() match {
                  case Eof   => Eof
                  case '~'   => Key.`end`
                  case other => Key.unknown(s"${Ascii.ESC}[4${other}")
                }
              case '5' =>
                read() match {
                  case Eof => Eof
                  case 'A' => Key.controlUp
                  case 'B' => Key.controlDown
                  case 'C' => Key.controlRight
                  case 'D' => Key.controlLeft
                  case '^' => Key.controlPageUp
                  case ';' =>
                    read() match {
                      case Eof => Eof
                      case '2' =>
                        readMatchOrUnknown(
                          '~',
                          Key.shiftPageUp,
                          s"${Ascii.ESC}[5;2"
                        )
                      case '5' =>
                        readMatchOrUnknown(
                          '~',
                          Key.controlPageUp,
                          s"${Ascii.ESC}[5;5"
                        )
                      case '6' =>
                        readMatchOrUnknown(
                          '~',
                          Key.controlShiftPageUp,
                          s"${Ascii.ESC}[5;6"
                        )
                      case other => Key.unknown(s"${Ascii.ESC}[5;${other}")
                    }
                  case '~'   => Key.pageUp
                  case other => Key.unknown(s"${Ascii.ESC}[5${other}")
                }
              case '6' =>
                read() match {
                  case Eof => Eof
                  case '^' => Key.controlPageDown
                  case ';' =>
                    read() match {
                      case Eof => Eof
                      case '2' =>
                        readMatchOrUnknown(
                          '~',
                          Key.shiftPageDown,
                          s"${Ascii.ESC}[6;2"
                        )
                      case '5' =>
                        readMatchOrUnknown(
                          '~',
                          Key.controlPageDown,
                          s"${Ascii.ESC}[6;5"
                        )
                      case '6' =>
                        readMatchOrUnknown(
                          '~',
                          Key.controlShiftPageDown,
                          s"${Ascii.ESC}[6;6"
                        )
                      case other => Key.unknown(s"${Ascii.ESC}[6;${other}")
                    }
                  case '~'   => Key.pageDown
                  case other => Key.unknown(s"${Ascii.ESC}[6${other}")
                }
              case '7' =>
                read() match {
                  case Eof   => Eof
                  case '^'   => Key.controlEnd
                  case '$'   => Key.shiftHome
                  case '~'   => Key.home
                  case other => Key.unknown(s"${Ascii.ESC}[7${other}")
                }
              case '8' =>
                read() match {
                  case Eof   => Eof
                  case '^'   => Key.controlHome
                  case '$'   => Key.shiftEnd
                  case '~'   => Key.`end`
                  case other => Key.unknown(s"${Ascii.ESC}[8${other}")
                }
              case '[' =>
                read() match {
                  case Eof         => Eof
                  case 'A'         => Key.f1
                  case 'B'         => Key.f2
                  case 'C'         => Key.f3
                  case 'D'         => Key.f4
                  case 'E'         => Key.f5
                  case other: Char => Key.unknown(s"${Ascii.ESC}[[${other}")
                }
              case ';' =>
                read() match {
                  case Eof   => Eof
                  case other => Key.unknown(s"${Ascii.ESC}[;{other}")
                }
              case '~'         => Key.backTab
              case other: Char => Key.unknown(s"${Ascii.ESC}[${other}")
            }
          // Application mode
          case 'O' =>
            read() match {
              case Eof         => Eof
              case 'a'         => Key.controlUp
              case 'b'         => Key.controlUp
              case 'c'         => Key.controlRight
              case 'd'         => Key.controlLeft
              case 'j'         => Key('*')
              case 'k'         => Key('+')
              case 'm'         => Key('-')
              case 'n'         => Key('.')
              case 'o'         => Key('/')
              case 'p'         => Key('0')
              case 'q'         => Key('1')
              case 'r'         => Key('2')
              case 's'         => Key('3')
              case 't'         => Key('4')
              case 'u'         => Key('5')
              case 'v'         => Key('6')
              case 'w'         => Key('7')
              case 'x'         => Key('8')
              case 'y'         => Key('9')
              case 'A'         => Key.up
              case 'B'         => Key.down
              case 'C'         => Key.right
              case 'D'         => Key.left
              case 'F'         => Key.`end`
              case 'H'         => Key.home
              case 'M'         => Key.enter
              case 'P'         => Key.f1
              case 'Q'         => Key.f2
              case 'R'         => Key.f3
              case 'S'         => Key.f4
              case other: Char => Key.unknown(s"${Ascii.ESC}O${other}")
            }

          case '§'         => Key('§')
          case '1'         => Key('¡')
          case '2'         => Key('™')
          case '3'         => Key('£')
          case '4'         => Key('¢')
          case '5'         => Key('∞')
          case '6'         => Key('§')
          case '7'         => Key('¶')
          case '8'         => Key('•')
          case '9'         => Key('ª')
          case '0'         => Key('º')
          case '-'         => Key('–')
          case '='         => Key('≠')
          case other: Char => Key.unknown(s"${Ascii.ESC}${other}")
        }

      case Eof         => Eof
      case other: Char => Key(other)
    }
}
