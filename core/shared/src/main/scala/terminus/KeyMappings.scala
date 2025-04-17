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

package terminus

import terminus.effect.Ascii

class KeySequence(val root: Key, val sequences: Map[String, Key]):
  val subSequences: Set[String] = sequences.keySet.flatMap(s =>
    if s.length <= 2 then Set.empty
    else (2 until s.length).map(s.substring(0, _)).toSet
  )

object KeyMappings:
  lazy val default: Map[Char, Key | KeySequence] = Map(
    ' ' -> Key.space,
    Ascii.NUL -> Key.controlAt, // Control-At (Also for Ctrl-Space)
    Ascii.SOH -> Key.controlA, // Control-A (home)
    Ascii.STX -> Key.controlB, // Control-B (emacs cursor left)
    Ascii.ETX -> Key.controlC, // Control-C (interrupt)
    Ascii.EOT -> Key.controlD, // Control-D (exit)
    Ascii.ENQ -> Key.controlE, // Control-E (end)
    Ascii.ACK -> Key.controlF, // Control-F (cursor forward)
    Ascii.BEL -> Key.controlG, // Control-G
    Ascii.BS -> Key.backspace, // Control-H (8) (Identical to '\b')
    Ascii.HT -> Key.tab, // Control-I (9) (Identical to '\t')
    Ascii.LF -> Key.newLine, // Control-J (10) (Identical to '\n')
    Ascii.VT -> Key.controlK, // Control-K (delete until end of line; vertical tab)
    Ascii.FF -> Key.controlL, // Control-L (clear; form feed)
    Ascii.CR -> Key.enter,
    Ascii.SO -> Key.controlN, // Control-N (14) (history forward)
    Ascii.SI -> Key.controlO, // Control-O (15)
    Ascii.DLE -> Key.controlP, // Control-P (16) (history back)
    Ascii.DC1 -> Key.controlQ, // Control-Q
    Ascii.DC2 -> Key.controlR, // Control-R (18) (reverse search)
    Ascii.DC3 -> Key.controlS, // Control-S (19) (forward search)
    Ascii.DC4 -> Key.controlT, // Control-T
    Ascii.NAK -> Key.controlU, // Control-U
    Ascii.SYN -> Key.controlV, // Control-V
    Ascii.ETB -> Key.controlW, // Control-W
    Ascii.CAN -> Key.controlX, // Control-X
    Ascii.EM -> Key.controlY, // Control-Y (25)
    Ascii.SUB -> Key.controlZ, // Control-Z

    '\u009b' -> Key.shiftEscape,
    '\u001c' -> Key.controlBackslash,
    '\u001d' -> Key.controlSquareClose,
    '\u001e' -> Key.controlCircumflex,
    '\u001f' -> Key.controlUnderscore,
    '\u007f' -> Key.backspace,
    Ascii.ESC -> KeySequence(Key.escape, escapeSequences)
  )
  
  import Ascii.ESC

  private lazy val escapeSequences: Map[String, Key] = Map(
    // Simple ESC sequences
    s"$ESC${Ascii.HT}" -> Key.backTab,
    s"${ESC}b" -> Key.controlLeft,
    s"${ESC}f" -> Key.controlRight,
    s"$ESC§" -> Key('§'),
    s"${ESC}1" -> Key('¡'),
    s"${ESC}2" -> Key('™'),
    s"${ESC}3" -> Key('£'),
    s"${ESC}4" -> Key('¢'),
    s"${ESC}5" -> Key('∞'),
    s"${ESC}6" -> Key('§'),
    s"${ESC}7" -> Key('¶'),
    s"${ESC}8" -> Key('•'),
    s"${ESC}9" -> Key('ª'),
    s"${ESC}0" -> Key('º'),
    s"$ESC-" -> Key('–'),
    s"$ESC=" -> Key('≠'),

    // Normal mode - basic sequences
    s"$ESC[a" -> Key.shiftUp,
    s"$ESC[b" -> Key.shiftDown,
    s"$ESC[c" -> Key.shiftRight,
    s"$ESC[d" -> Key.shiftLeft,
    s"$ESC[A" -> Key.up,
    s"$ESC[B" -> Key.down,
    s"$ESC[C" -> Key.right,
    s"$ESC[D" -> Key.left,
    s"$ESC[F" -> Key.`end`,
    s"$ESC[H" -> Key.home,
    s"$ESC[Z" -> Key.backTab,
    s"$ESC[~" -> Key.backTab,

    // Function keys and control combinations
    s"$ESC[11^" -> Key.controlF1,
    s"$ESC[11~" -> Key.f1,
    s"$ESC[12^" -> Key.controlF2,
    s"$ESC[12~" -> Key.f2,
    s"$ESC[13^" -> Key.controlF3,
    s"$ESC[13~" -> Key.f3,
    s"$ESC[14^" -> Key.controlF4,
    s"$ESC[14~" -> Key.f4,
    s"$ESC[15^" -> Key.controlF5,
    s"$ESC[15~" -> Key.f5,
    s"$ESC[15;2~" -> Key.f17,
    s"$ESC[15;5~" -> Key.controlF5,
    s"$ESC[15;6~" -> Key.controlF17,
    s"$ESC[17^" -> Key.controlF6,
    s"$ESC[17~" -> Key.f6,
    s"$ESC[17;2~" -> Key.f18,
    s"$ESC[17;5~" -> Key.controlF6,
    s"$ESC[17;6~" -> Key.controlF18,
    s"$ESC[18^" -> Key.controlF7,
    s"$ESC[18~" -> Key.f7,
    s"$ESC[18;2~" -> Key.f19,
    s"$ESC[18;5~" -> Key.controlF7,
    s"$ESC[18;6~" -> Key.controlF19,
    s"$ESC[19^" -> Key.controlF8,
    s"$ESC[19~" -> Key.f8,
    s"$ESC[19;2~" -> Key.f20,
    s"$ESC[19;5~" -> Key.controlF8,
    s"$ESC[19;6~" -> Key.controlF20,

    // Special key combinations
    s"$ESC[1~" -> Key.home,
    s"$ESC[1;2A" -> Key.shiftUp,
    s"$ESC[1;2B" -> Key.shiftDown,
    s"$ESC[1;2C" -> Key.shiftRight,
    s"$ESC[1;2D" -> Key.shiftLeft,
    s"$ESC[1;2F" -> Key.shiftEnd,
    s"$ESC[1;2H" -> Key.shiftHome,
    s"$ESC[1;2P" -> Key.f13,
    s"$ESC[1;2Q" -> Key.f14,
    s"$ESC[1;2R" -> Key.f15,
    s"$ESC[1;2S" -> Key.f16,
    s"$ESC[1;5A" -> Key.controlUp,
    s"$ESC[1;5B" -> Key.controlDown,
    s"$ESC[1;5C" -> Key.controlRight,
    s"$ESC[1;5D" -> Key.controlLeft,
    s"$ESC[1;5F" -> Key.controlEnd,
    s"$ESC[1;5H" -> Key.controlHome,
    s"$ESC[1;5P" -> Key.controlF1,
    s"$ESC[1;5Q" -> Key.controlF2,
    s"$ESC[1;5R" -> Key.controlF3,
    s"$ESC[1;5S" -> Key.controlF4,
    s"$ESC[1;6A" -> Key.controlShiftUp,
    s"$ESC[1;6B" -> Key.controlShiftDown,
    s"$ESC[1;6C" -> Key.controlShiftRight,
    s"$ESC[1;6D" -> Key.controlShiftLeft,
    s"$ESC[1;6F" -> Key.controlShiftEnd,
    s"$ESC[1;6H" -> Key.controlShiftHome,
    s"$ESC[1;6P" -> Key.controlF13,
    s"$ESC[1;6Q" -> Key.controlF14,
    s"$ESC[1;6R" -> Key.controlF15,
    s"$ESC[1;6S" -> Key.controlF16,

    // Function keys 9-24
    s"$ESC[20^" -> Key.controlF9,
    s"$ESC[20~" -> Key.f9,
    s"$ESC[20;2~" -> Key.f21,
    s"$ESC[20;5~" -> Key.controlF9,
    s"$ESC[20;6~" -> Key.controlF21,
    s"$ESC[21^" -> Key.controlF10,
    s"$ESC[21~" -> Key.f10,
    s"$ESC[21;2~" -> Key.f22,
    s"$ESC[21;5~" -> Key.controlF10,
    s"$ESC[21;6~" -> Key.controlF22,
    s"$ESC[23@" -> Key.controlF21,
    s"$ESC[23^" -> Key.controlF11,
    s"$ESC[23$$" -> Key.f23,
    s"$ESC[23~" -> Key.f11,
    s"$ESC[23;2~" -> Key.f23,
    s"$ESC[23;5~" -> Key.controlF11,
    s"$ESC[23;6~" -> Key.controlF23,
    s"$ESC[24@" -> Key.controlF22,
    s"$ESC[24^" -> Key.controlF12,
    s"$ESC[24$$" -> Key.f24,
    s"$ESC[24~" -> Key.f12,
    s"$ESC[24;2~" -> Key.f24,
    s"$ESC[24;5~" -> Key.controlF12,
    s"$ESC[24;6~" -> Key.controlF24,
    s"$ESC[25^" -> Key.controlF13,
    s"$ESC[25~" -> Key.f13,
    s"$ESC[26^" -> Key.controlF14,
    s"$ESC[26~" -> Key.f14,
    s"$ESC[28^" -> Key.controlF15,
    s"$ESC[28~" -> Key.f15,
    s"$ESC[29^" -> Key.controlF16,
    s"$ESC[29~" -> Key.f16,
    s"$ESC[31^" -> Key.controlF17,
    s"$ESC[31~" -> Key.f17,
    s"$ESC[32^" -> Key.controlF18,
    s"$ESC[32~" -> Key.f18,
    s"$ESC[33^" -> Key.controlF19,
    s"$ESC[33~" -> Key.f19,
    s"$ESC[34^" -> Key.controlF20,
    s"$ESC[34~" -> Key.f20,

    // Insert, Delete, Page Up/Down
    s"$ESC[2~" -> Key.insert,
    s"$ESC[3^" -> Key.controlDelete,
    s"$ESC[3$$" -> Key.shiftDelete,
    s"$ESC[3~" -> Key.delete,
    s"$ESC[3;2~" -> Key.shiftDelete,
    s"$ESC[3;5~" -> Key.controlDelete,
    s"$ESC[3;6~" -> Key.controlShiftDelete,
    s"$ESC[4~" -> Key.`end`,
    s"$ESC[5A" -> Key.controlUp,
    s"$ESC[5B" -> Key.controlDown,
    s"$ESC[5C" -> Key.controlRight,
    s"$ESC[5D" -> Key.controlLeft,
    s"$ESC[5^" -> Key.controlPageUp,
    s"$ESC[5~" -> Key.pageUp,
    s"$ESC[5;2~" -> Key.shiftPageUp,
    s"$ESC[5;5~" -> Key.controlPageUp,
    s"$ESC[5;6~" -> Key.controlShiftPageUp,
    s"$ESC[6^" -> Key.controlPageDown,
    s"$ESC[6~" -> Key.pageDown,
    s"$ESC[6;2~" -> Key.shiftPageDown,
    s"$ESC[6;5~" -> Key.controlPageDown,
    s"$ESC[6;6~" -> Key.controlShiftPageDown,
    s"$ESC[7^" -> Key.controlEnd,
    s"$ESC[7$$" -> Key.shiftHome,
    s"$ESC[7~" -> Key.home,
    s"$ESC[8^" -> Key.controlHome,
    s"$ESC[8$$" -> Key.shiftEnd,
    s"$ESC[8~" -> Key.`end`,

    // Function key shortcuts
    s"$ESC[[A" -> Key.f1,
    s"$ESC[[B" -> Key.f2,
    s"$ESC[[C" -> Key.f3,
    s"$ESC[[D" -> Key.f4,
    s"$ESC[[E" -> Key.f5,

    // Application mode
    s"${ESC}Oa" -> Key.controlUp,
    s"${ESC}Ob" -> Key.controlUp,
    s"${ESC}Oc" -> Key.controlRight,
    s"${ESC}Od" -> Key.controlLeft,
    s"${ESC}Oj" -> Key('*'),
    s"${ESC}Ok" -> Key('+'),
    s"${ESC}Om" -> Key('-'),
    s"${ESC}On" -> Key('.'),
    s"${ESC}Oo" -> Key('/'),
    s"${ESC}Op" -> Key('0'),
    s"${ESC}Oq" -> Key('1'),
    s"${ESC}Or" -> Key('2'),
    s"${ESC}Os" -> Key('3'),
    s"${ESC}Ot" -> Key('4'),
    s"${ESC}Ou" -> Key('5'),
    s"${ESC}Ov" -> Key('6'),
    s"${ESC}Ow" -> Key('7'),
    s"${ESC}Ox" -> Key('8'),
    s"${ESC}Oy" -> Key('9'),
    s"${ESC}OA" -> Key.up,
    s"${ESC}OB" -> Key.down,
    s"${ESC}OC" -> Key.right,
    s"${ESC}OD" -> Key.left,
    s"${ESC}OF" -> Key.`end`,
    s"${ESC}OH" -> Key.home,
    s"${ESC}OM" -> Key.enter,
    s"${ESC}OP" -> Key.f1,
    s"${ESC}OQ" -> Key.f2,
    s"${ESC}OR" -> Key.f3,
    s"${ESC}OS" -> Key.f4
  )
