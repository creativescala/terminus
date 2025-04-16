package terminus

import terminus.effect.Ascii

class KeySequence(val root: Key, val sequences: Map[String, Key]):
  val subSequences: Set[String] = sequences.keySet.flatMap(s =>
    if s.length <= 2 then Set.empty
    else (2 until s.length).map(s.substring(0, _)).toSet)

object KeyMappings:
  lazy val escapeSequences: Map[String, Key] = Map(
    // Simple ESC sequences
    s"${Ascii.ESC}${Ascii.HT}" -> Key.backTab,
    s"${Ascii.ESC}b" -> Key.controlLeft,
    s"${Ascii.ESC}f" -> Key.controlRight,
    s"${Ascii.ESC}§" -> Key('§'),
    s"${Ascii.ESC}1" -> Key('¡'),
    s"${Ascii.ESC}2" -> Key('™'),
    s"${Ascii.ESC}3" -> Key('£'),
    s"${Ascii.ESC}4" -> Key('¢'),
    s"${Ascii.ESC}5" -> Key('∞'),
    s"${Ascii.ESC}6" -> Key('§'),
    s"${Ascii.ESC}7" -> Key('¶'),
    s"${Ascii.ESC}8" -> Key('•'),
    s"${Ascii.ESC}9" -> Key('ª'),
    s"${Ascii.ESC}0" -> Key('º'),
    s"${Ascii.ESC}-" -> Key('–'),
    s"${Ascii.ESC}=" -> Key('≠'),

    // Normal mode - basic sequences
    s"${Ascii.ESC}[a" -> Key.shiftUp,
    s"${Ascii.ESC}[b" -> Key.shiftDown,
    s"${Ascii.ESC}[c" -> Key.shiftRight,
    s"${Ascii.ESC}[d" -> Key.shiftLeft,
    s"${Ascii.ESC}[A" -> Key.up,
    s"${Ascii.ESC}[B" -> Key.down,
    s"${Ascii.ESC}[C" -> Key.right,
    s"${Ascii.ESC}[D" -> Key.left,
    s"${Ascii.ESC}[F" -> Key.`end`,
    s"${Ascii.ESC}[H" -> Key.home,
    s"${Ascii.ESC}[Z" -> Key.backTab,
    s"${Ascii.ESC}[~" -> Key.backTab,

    // Function keys and control combinations
    s"${Ascii.ESC}[11^" -> Key.controlF1,
    s"${Ascii.ESC}[11~" -> Key.f1,
    s"${Ascii.ESC}[12^" -> Key.controlF2,
    s"${Ascii.ESC}[12~" -> Key.f2,
    s"${Ascii.ESC}[13^" -> Key.controlF3,
    s"${Ascii.ESC}[13~" -> Key.f3,
    s"${Ascii.ESC}[14^" -> Key.controlF4,
    s"${Ascii.ESC}[14~" -> Key.f4,
    s"${Ascii.ESC}[15^" -> Key.controlF5,
    s"${Ascii.ESC}[15~" -> Key.f5,
    s"${Ascii.ESC}[15;2~" -> Key.f17,
    s"${Ascii.ESC}[15;5~" -> Key.controlF5,
    s"${Ascii.ESC}[15;6~" -> Key.controlF17,
    s"${Ascii.ESC}[17^" -> Key.controlF6,
    s"${Ascii.ESC}[17~" -> Key.f6,
    s"${Ascii.ESC}[17;2~" -> Key.f18,
    s"${Ascii.ESC}[17;5~" -> Key.controlF6,
    s"${Ascii.ESC}[17;6~" -> Key.controlF18,
    s"${Ascii.ESC}[18^" -> Key.controlF7,
    s"${Ascii.ESC}[18~" -> Key.f7,
    s"${Ascii.ESC}[18;2~" -> Key.f19,
    s"${Ascii.ESC}[18;5~" -> Key.controlF7,
    s"${Ascii.ESC}[18;6~" -> Key.controlF19,
    s"${Ascii.ESC}[19^" -> Key.controlF8,
    s"${Ascii.ESC}[19~" -> Key.f8,
    s"${Ascii.ESC}[19;2~" -> Key.f20,
    s"${Ascii.ESC}[19;5~" -> Key.controlF8,
    s"${Ascii.ESC}[19;6~" -> Key.controlF20,

    // Special key combinations
    s"${Ascii.ESC}[1~" -> Key.home,
    s"${Ascii.ESC}[1;2A" -> Key.shiftUp,
    s"${Ascii.ESC}[1;2B" -> Key.shiftDown,
    s"${Ascii.ESC}[1;2C" -> Key.shiftRight,
    s"${Ascii.ESC}[1;2D" -> Key.shiftLeft,
    s"${Ascii.ESC}[1;2F" -> Key.shiftEnd,
    s"${Ascii.ESC}[1;2H" -> Key.shiftHome,
    s"${Ascii.ESC}[1;2P" -> Key.f13,
    s"${Ascii.ESC}[1;2Q" -> Key.f14,
    s"${Ascii.ESC}[1;2R" -> Key.f15,
    s"${Ascii.ESC}[1;2S" -> Key.f16,
    s"${Ascii.ESC}[1;5A" -> Key.controlUp,
    s"${Ascii.ESC}[1;5B" -> Key.controlDown,
    s"${Ascii.ESC}[1;5C" -> Key.controlRight,
    s"${Ascii.ESC}[1;5D" -> Key.controlLeft,
    s"${Ascii.ESC}[1;5F" -> Key.controlEnd,
    s"${Ascii.ESC}[1;5H" -> Key.controlHome,
    s"${Ascii.ESC}[1;5P" -> Key.controlF1,
    s"${Ascii.ESC}[1;5Q" -> Key.controlF2,
    s"${Ascii.ESC}[1;5R" -> Key.controlF3,
    s"${Ascii.ESC}[1;5S" -> Key.controlF4,
    s"${Ascii.ESC}[1;6A" -> Key.controlShiftUp,
    s"${Ascii.ESC}[1;6B" -> Key.controlShiftDown,
    s"${Ascii.ESC}[1;6C" -> Key.controlShiftRight,
    s"${Ascii.ESC}[1;6D" -> Key.controlShiftLeft,
    s"${Ascii.ESC}[1;6F" -> Key.controlShiftEnd,
    s"${Ascii.ESC}[1;6H" -> Key.controlShiftHome,
    s"${Ascii.ESC}[1;6P" -> Key.controlF13,
    s"${Ascii.ESC}[1;6Q" -> Key.controlF14,
    s"${Ascii.ESC}[1;6R" -> Key.controlF15,
    s"${Ascii.ESC}[1;6S" -> Key.controlF16,

    // Function keys 9-24
    s"${Ascii.ESC}[20^" -> Key.controlF9,
    s"${Ascii.ESC}[20~" -> Key.f9,
    s"${Ascii.ESC}[20;2~" -> Key.f21,
    s"${Ascii.ESC}[20;5~" -> Key.controlF9,
    s"${Ascii.ESC}[20;6~" -> Key.controlF21,
    s"${Ascii.ESC}[21^" -> Key.controlF10,
    s"${Ascii.ESC}[21~" -> Key.f10,
    s"${Ascii.ESC}[21;2~" -> Key.f22,
    s"${Ascii.ESC}[21;5~" -> Key.controlF10,
    s"${Ascii.ESC}[21;6~" -> Key.controlF22,
    s"${Ascii.ESC}[23@" -> Key.controlF21,
    s"${Ascii.ESC}[23^" -> Key.controlF11,
    s"${Ascii.ESC}[23$$" -> Key.f23,
    s"${Ascii.ESC}[23~" -> Key.f11,
    s"${Ascii.ESC}[23;2~" -> Key.f23,
    s"${Ascii.ESC}[23;5~" -> Key.controlF11,
    s"${Ascii.ESC}[23;6~" -> Key.controlF23,
    s"${Ascii.ESC}[24@" -> Key.controlF22,
    s"${Ascii.ESC}[24^" -> Key.controlF12,
    s"${Ascii.ESC}[24$$" -> Key.f24,
    s"${Ascii.ESC}[24~" -> Key.f12,
    s"${Ascii.ESC}[24;2~" -> Key.f24,
    s"${Ascii.ESC}[24;5~" -> Key.controlF12,
    s"${Ascii.ESC}[24;6~" -> Key.controlF24,
    s"${Ascii.ESC}[25^" -> Key.controlF13,
    s"${Ascii.ESC}[25~" -> Key.f13,
    s"${Ascii.ESC}[26^" -> Key.controlF14,
    s"${Ascii.ESC}[26~" -> Key.f14,
    s"${Ascii.ESC}[28^" -> Key.controlF15,
    s"${Ascii.ESC}[28~" -> Key.f15,
    s"${Ascii.ESC}[29^" -> Key.controlF16,
    s"${Ascii.ESC}[29~" -> Key.f16,

    // Insert, Delete, Page Up/Down
    s"${Ascii.ESC}[2~" -> Key.insert,
    s"${Ascii.ESC}[3^" -> Key.controlDelete,
    s"${Ascii.ESC}[3$$" -> Key.shiftDelete,
    s"${Ascii.ESC}[3~" -> Key.delete,
    s"${Ascii.ESC}[3;2~" -> Key.shiftDelete,
    s"${Ascii.ESC}[3;5~" -> Key.controlDelete,
    s"${Ascii.ESC}[3;6~" -> Key.controlShiftDelete,
    s"${Ascii.ESC}[4~" -> Key.`end`,
    s"${Ascii.ESC}[5A" -> Key.controlUp,
    s"${Ascii.ESC}[5B" -> Key.controlDown,
    s"${Ascii.ESC}[5C" -> Key.controlRight,
    s"${Ascii.ESC}[5D" -> Key.controlLeft,
    s"${Ascii.ESC}[5^" -> Key.controlPageUp,
    s"${Ascii.ESC}[5~" -> Key.pageUp,
    s"${Ascii.ESC}[5;2~" -> Key.shiftPageUp,
    s"${Ascii.ESC}[5;5~" -> Key.controlPageUp,
    s"${Ascii.ESC}[5;6~" -> Key.controlShiftPageUp,
    s"${Ascii.ESC}[6^" -> Key.controlPageDown,
    s"${Ascii.ESC}[6~" -> Key.pageDown,
    s"${Ascii.ESC}[6;2~" -> Key.shiftPageDown,
    s"${Ascii.ESC}[6;5~" -> Key.controlPageDown,
    s"${Ascii.ESC}[6;6~" -> Key.controlShiftPageDown,
    s"${Ascii.ESC}[7^" -> Key.controlEnd,
    s"${Ascii.ESC}[7$$" -> Key.shiftHome,
    s"${Ascii.ESC}[7~" -> Key.home,
    s"${Ascii.ESC}[8^" -> Key.controlHome,
    s"${Ascii.ESC}[8$$" -> Key.shiftEnd,
    s"${Ascii.ESC}[8~" -> Key.`end`,

    // Function key shortcuts
    s"${Ascii.ESC}[[A" -> Key.f1,
    s"${Ascii.ESC}[[B" -> Key.f2,
    s"${Ascii.ESC}[[C" -> Key.f3,
    s"${Ascii.ESC}[[D" -> Key.f4,
    s"${Ascii.ESC}[[E" -> Key.f5,

    // Application mode
    s"${Ascii.ESC}Oa" -> Key.controlUp,
    s"${Ascii.ESC}Ob" -> Key.controlUp,
    s"${Ascii.ESC}Oc" -> Key.controlRight,
    s"${Ascii.ESC}Od" -> Key.controlLeft,
    s"${Ascii.ESC}Oj" -> Key('*'),
    s"${Ascii.ESC}Ok" -> Key('+'),
    s"${Ascii.ESC}Om" -> Key('-'),
    s"${Ascii.ESC}On" -> Key('.'),
    s"${Ascii.ESC}Oo" -> Key('/'),
    s"${Ascii.ESC}Op" -> Key('0'),
    s"${Ascii.ESC}Oq" -> Key('1'),
    s"${Ascii.ESC}Or" -> Key('2'),
    s"${Ascii.ESC}Os" -> Key('3'),
    s"${Ascii.ESC}Ot" -> Key('4'),
    s"${Ascii.ESC}Ou" -> Key('5'),
    s"${Ascii.ESC}Ov" -> Key('6'),
    s"${Ascii.ESC}Ow" -> Key('7'),
    s"${Ascii.ESC}Ox" -> Key('8'),
    s"${Ascii.ESC}Oy" -> Key('9'),
    s"${Ascii.ESC}OA" -> Key.up,
    s"${Ascii.ESC}OB" -> Key.down,
    s"${Ascii.ESC}OC" -> Key.right,
    s"${Ascii.ESC}OD" -> Key.left,
    s"${Ascii.ESC}OF" -> Key.`end`,
    s"${Ascii.ESC}OH" -> Key.home,
    s"${Ascii.ESC}OM" -> Key.enter,
    s"${Ascii.ESC}OP" -> Key.f1,
    s"${Ascii.ESC}OQ" -> Key.f2,
    s"${Ascii.ESC}OR" -> Key.f3,
    s"${Ascii.ESC}OS" -> Key.f4
  )

  lazy val default: Map[Char, Key | KeySequence] = Map(
    ' '       -> Key.space,
    Ascii.NUL -> Key.controlAt, // Control-At (Also for Ctrl-Space)
    Ascii.SOH -> Key.controlA, // Control-A (home)
    Ascii.STX -> Key.controlB, // Control-B (emacs cursor left)
    Ascii.ETX -> Key.controlC, // Control-C (interrupt)
    Ascii.EOT -> Key.controlD, // Control-D (exit)
    Ascii.ENQ -> Key.controlE, // Control-E (end)
    Ascii.ACK -> Key.controlF, // Control-F (cursor forward)
    Ascii.BEL -> Key.controlG, // Control-G
    Ascii.BS  -> Key.backspace, // Control-H (8) (Identical to '\b')
    Ascii.HT  -> Key.tab, // Control-I (9) (Identical to '\t')
    Ascii.LF  -> Key.newLine, // Control-J (10) (Identical to '\n')
    Ascii.VT  -> Key.controlK, // Control-K (delete until end of line; vertical tab)
    Ascii.FF  -> Key.controlL, // Control-L (clear; form feed)
    Ascii.CR  -> Key.enter,
    Ascii.SO  -> Key.controlN, // Control-N (14) (history forward)
    Ascii.SI  -> Key.controlO, // Control-O (15)
    Ascii.DLE -> Key.controlP, // Control-P (16) (history back)
    Ascii.DC1 -> Key.controlQ, // Control-Q
    Ascii.DC2 -> Key.controlR, // Control-R (18) (reverse search)
    Ascii.DC3 -> Key.controlS, // Control-S (19) (forward search)
    Ascii.DC4 -> Key.controlT, // Control-T
    Ascii.NAK -> Key.controlU, // Control-U
    Ascii.SYN -> Key.controlV, // Control-V
    Ascii.ETB -> Key.controlW, // Control-W
    Ascii.CAN -> Key.controlX, // Control-X
    Ascii.EM  -> Key.controlY, // Control-Y (25)
    Ascii.SUB -> Key.controlZ, // Control-Z

    '\u009b' -> Key.shiftEscape,
    '\u001c' -> Key.controlBackslash,
    '\u001d' -> Key.controlSquareClose,
    '\u001e' -> Key.controlCircumflex,
    '\u001f' -> Key.controlUnderscore,
    '\u007f' -> Key.backspace,
    Ascii.ESC -> KeySequence(Key.escape, escapeSequences)
  )
