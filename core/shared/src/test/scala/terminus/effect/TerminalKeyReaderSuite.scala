package terminus.effect

import munit.FunSuite
import terminus.Key

class TerminalKeyReaderSuite extends FunSuite {
  test("The expected key is generated given the input") {
    // The following list is taken from Textual, with some sequences edited out
    //   https://github.com/Textualize/textual/blob/main/src/textual/_ansi_sequences.py
    val codes: Seq[(String, Key)] =
      Seq(
        (" ") -> (Key.space),
        ("\r") -> (Key.enter),
        ("\u0000") -> (Key.controlAt),
        ("\u0001") -> (Key.controlA),
        ("\u0002") -> (Key.controlB),
        ("\u0003") -> (Key.controlC),
        ("\u0004") -> (Key.controlD),
        ("\u0005") -> (Key.controlE),
        ("\u0006") -> (Key.controlF),
        ("\u0007") -> (Key.controlG),
        ("\u0008") -> (Key.backspace),
        ("\u0009") -> (Key.tab),
        ("\u000a") -> (Key.newLine),
        ("\u000b") -> (Key.controlK),
        ("\u000c") -> (Key.controlL),
        ("\u000e") -> (Key.controlN),
        ("\u000f") -> (Key.controlO),
        ("\u0010") -> (Key.controlP),
        ("\u0011") -> (Key.controlQ),
        ("\u0012") -> (Key.controlR),
        ("\u0013") -> (Key.controlS),
        ("\u0014") -> (Key.controlT),
        ("\u0015") -> (Key.controlU),
        ("\u0016") -> (Key.controlV),
        ("\u0017") -> (Key.controlW),
        ("\u0018") -> (Key.controlX),
        ("\u0019") -> (Key.controlY),
        ("\u001a") -> (Key.controlZ),
        ("\u001b") -> (Key.escape),
        "\u009b" -> (Key.shiftEscape),
        "\u001c" -> (Key.controlBackslash), // Both Control-\ (also Ctrl-| )
        "\u001d" -> (Key.controlSquareClose), // Control-]
        "\u001e" -> (Key.controlCircumflex), // Control-^
        "\u001f" -> (Key.controlUnderscore), // Control-underscore (Also for Ctrl-hyphen.)
        "\u001b[1~" -> (Key.home), // tmux
        "\u001b[2~" -> (Key.insert),
        "\u001b[3~" -> (Key.delete),
        "\u001b[4~" -> (Key.`end`), // tmux
        "\u001b[5~" -> (Key.pageUp),
        "\u001b[6~" -> (Key.pageDown),
        "\u001b[7~" -> (Key.home), // xrvt
        "\u001b[8~" -> (Key.`end`), // xrvt
        "\u001b[Z" -> (Key.backTab), // shift + tab
        "\u001b\u0009" -> (Key.backTab), // Linux console
        "\u001b[~" -> (Key.backTab), // Windows console
        "\u001bOP" -> (Key.f1),
        "\u001bOQ" -> (Key.f2),
        "\u001bOR" -> (Key.f3),
        "\u001bOS" -> (Key.f4),
        "\u001b[[A" -> (Key.f1), // Linux console.
        "\u001b[[B" -> (Key.f2), // Linux console.
        "\u001b[[C" -> (Key.f3), // Linux console.
        "\u001b[[D" -> (Key.f4), // Linux console.
        "\u001b[[E" -> (Key.f5), // Linux console.
        "\u001b[11~" -> (Key.f1), // rxvt-unicode
        "\u001b[12~" -> (Key.f2), // rxvt-unicode
        "\u001b[13~" -> (Key.f3), // rxvt-unicode
        "\u001b[14~" -> (Key.f4), // rxvt-unicode
        "\u001b[15~" -> (Key.f5),
        "\u001b[17~" -> (Key.f6),
        "\u001b[18~" -> (Key.f7),
        "\u001b[19~" -> (Key.f8),
        "\u001b[20~" -> (Key.f9),
        "\u001b[21~" -> (Key.f10),
        "\u001b[23~" -> (Key.f11),
        "\u001b[24~" -> (Key.f12),
        "\u001b[25~" -> (Key.f13),
        "\u001b[26~" -> (Key.f14),
        "\u001b[28~" -> (Key.f15),
        "\u001b[29~" -> (Key.f16),
        "\u001b[31~" -> (Key.f17),
        "\u001b[32~" -> (Key.f18),
        "\u001b[33~" -> (Key.f19),
        "\u001b[34~" -> (Key.f20),
        // Xterm
        "\u001b[1;2P" -> (Key.f13),
        "\u001b[1;2Q" -> (Key.f14),
        "\u001b[1;2R" -> (Key.f15),
        "\u001b[1;2S" -> (Key.f16),
        "\u001b[15;2~" -> (Key.f17),
        "\u001b[17;2~" -> (Key.f18),
        "\u001b[18;2~" -> (Key.f19),
        "\u001b[19;2~" -> (Key.f20),
        "\u001b[20;2~" -> (Key.f21),
        "\u001b[21;2~" -> (Key.f22),
        "\u001b[23;2~" -> (Key.f23),
        "\u001b[24;2~" -> (Key.f24),
        "\u001b[23$" -> (Key.f23), // rxvt
        "\u001b[24$" -> (Key.f24), // rxvt
        // --
        // Control + function keys.
        "\u001b[1;5P" -> (Key.controlF1),
        "\u001b[1;5Q" -> (Key.controlF2),
        "\u001b[1;5R" -> (Key.controlF3),
        "\u001b[1;5S" -> (Key.controlF4),
        "\u001b[15;5~" -> (Key.controlF5),
        "\u001b[17;5~" -> (Key.controlF6),
        "\u001b[18;5~" -> (Key.controlF7),
        "\u001b[19;5~" -> (Key.controlF8),
        "\u001b[20;5~" -> (Key.controlF9),
        "\u001b[21;5~" -> (Key.controlF10),
        "\u001b[23;5~" -> (Key.controlF11),
        "\u001b[24;5~" -> (Key.controlF12),
        "\u001b[1;6P" -> (Key.controlF13),
        "\u001b[1;6Q" -> (Key.controlF14),
        "\u001b[1;6R" -> (Key.controlF15), // Conflicts with CPR response; enabled after https ->/github.com/Textualize/textual/issues/3440.
        "\u001b[1;6S" -> (Key.controlF16),
        "\u001b[15;6~" -> (Key.controlF17),
        "\u001b[17;6~" -> (Key.controlF18),
        "\u001b[18;6~" -> (Key.controlF19),
        "\u001b[19;6~" -> (Key.controlF20),
        "\u001b[20;6~" -> (Key.controlF21),
        "\u001b[21;6~" -> (Key.controlF22),
        "\u001b[23;6~" -> (Key.controlF23),
        "\u001b[24;6~" -> (Key.controlF24),
        // rxvt-unicode control function key -s
        "\u001b[11^" -> (Key.controlF1),
        "\u001b[12^" -> (Key.controlF2),
        "\u001b[13^" -> (Key.controlF3),
        "\u001b[14^" -> (Key.controlF4),
        "\u001b[15^" -> (Key.controlF5),
        "\u001b[17^" -> (Key.controlF6),
        "\u001b[18^" -> (Key.controlF7),
        "\u001b[19^" -> (Key.controlF8),
        "\u001b[20^" -> (Key.controlF9),
        "\u001b[21^" -> (Key.controlF10),
        "\u001b[23^" -> (Key.controlF11),
        "\u001b[24^" -> (Key.controlF12),
        // rxvt-unicode control+shift function key -s
        "\u001b[25^" -> (Key.controlF13),
        "\u001b[26^" -> (Key.controlF14),
        "\u001b[28^" -> (Key.controlF15),
        "\u001b[29^" -> (Key.controlF16),
        "\u001b[31^" -> (Key.controlF17),
        "\u001b[32^" -> (Key.controlF18),
        "\u001b[33^" -> (Key.controlF19),
        "\u001b[34^" -> (Key.controlF20),
        "\u001b[23@" -> (Key.controlF21),
        "\u001b[24@" -> (Key.controlF22),
        // --
        // Tmux (Win32 subsystem) sends the following scroll events.
        // "\u001b[62~" -> (Key.scrollUp),
        // "\u001b[63~" -> (Key.scrollDown),
        // Meta/control/escape + pageup/pagedown/insert/delete.
        "\u001b[3;2~" -> (Key.shiftDelete), // xterm, gnome-terminal.
        "\u001b[3$" -> (Key.shiftDelete), // rxvt
        "\u001b[5;2~" -> (Key.shiftPageUp),
        "\u001b[6;2~" -> (Key.shiftPageDown),
        // "\u001b[2;3~" -> (Key.Escape, Key.insert),
        // "\u001b[3;3~" -> (Key.Escape, Key.delete),
        // "\u001b[5;3~" -> (Key.Escape, Key.pageUp),
        // "\u001b[6;3~" -> (Key.Escape, Key.pageDown),
        // "\u001b[2;4~" -> (Key.Escape, Key.shiftInsert),
        // "\u001b[3;4~" -> (Key.Escape, Key.shiftDelete),
        // "\u001b[5;4~" -> (Key.Escape, Key.shiftPageUp),
        // "\u001b[6;4~" -> (Key.Escape, Key.shiftPageDown),
        "\u001b[3;5~" -> (Key.controlDelete), // xterm, gnome-terminal.
        "\u001b[3^" -> (Key.controlDelete), // rxvt
        "\u001b[5;5~" -> (Key.controlPageUp),
        "\u001b[6;5~" -> (Key.controlPageDown),
        "\u001b[5^" -> (Key.controlPageUp), // rxvt
        "\u001b[6^" -> (Key.controlPageDown), // rxvt
        "\u001b[3;6~" -> (Key.controlShiftDelete),
        "\u001b[5;6~" -> (Key.controlShiftPageUp),
        "\u001b[6;6~" -> (Key.controlShiftPageDown),
        // "\u001b[2;7~" -> (Key.Escape, Key.controlInsert),
        // "\u001b[5;7~" -> (Key.Escape, Key.controlPageDown),
        // "\u001b[6;7~" -> (Key.Escape, Key.controlPageDown),
        // "\u001b[2;8~" -> (Key.Escape, Key.controlShiftInsert),
        // "\u001b[5;8~" -> (Key.Escape, Key.controlShiftPageDown),
        // "\u001b[6;8~" -> (Key.Escape, Key.controlShiftPageDown),
        // --
        // Arrows.
        // (Normal cursor mode).
        "\u001b[A" -> (Key.up),
        "\u001b[B" -> (Key.down),
        "\u001b[C" -> (Key.right),
        "\u001b[D" -> (Key.left),
        "\u001b[H" -> (Key.home),
        "\u001b[F" -> (Key.`end`),
        // Tmux sends following keystrokes when control+arrow is pressed, but for
        // Emacs ansi-term sends the same sequences for normal arrow keys. Consider
        // it a normal arrow press, because that's more important.
        // (Application cursor mode).
        "\u001bOA" -> (Key.up),
        "\u001bOB" -> (Key.down),
        "\u001bOC" -> (Key.right),
        "\u001bOD" -> (Key.left),
        "\u001bOF" -> (Key.`end`),
        "\u001bOH" -> (Key.home),
        // Shift + arrows.
        "\u001b[1;2A" -> (Key.shiftUp),
        "\u001b[1;2B" -> (Key.shiftDown),
        "\u001b[1;2C" -> (Key.shiftRight),
        "\u001b[1;2D" -> (Key.shiftLeft),
        "\u001b[1;2F" -> (Key.shiftEnd),
        "\u001b[1;2H" -> (Key.shiftHome),
        // Shift+navigation in rxvt
        "\u001b[a" -> (Key.shiftUp),
        "\u001b[b" -> (Key.shiftDown),
        "\u001b[c" -> (Key.shiftRight),
        "\u001b[d" -> (Key.shiftLeft),
        "\u001b[7$" -> (Key.shiftHome),
        "\u001b[8$" -> (Key.shiftEnd),
        // Meta + arrow keys. Several terminals handle this differently.
        // The following sequences are for xterm and gnome-terminal.
        //     (Iterm sends ESC followed by the normal arrow_up/down/left/right
        //     sequences, and the OSX Terminal sends ESCb and ESCf for "alt
        //     arrow_left" and "alt arrow_right." We don't handle these
        //     explicitly, in here, because would could not distinguish between
        //     pressing ESC (to go to Vi navigation mode), followed by just the
        //     'b' or 'f' key. These combinations are handled in
        //     the input processor.)
        // "\u001b[1;3A" -> (Key.Escape, Key.up),
        // "\u001b[1;3B" -> (Key.Escape, Key.down),
        // "\u001b[1;3C" -> (Key.Escape, Key.right),
        // "\u001b[1;3D" -> (Key.Escape, Key.left),
        // "\u001b[1;3F" -> (Key.Escape, Key.`end`),
        // "\u001b[1;3H" -> (Key.Escape, Key.home),
        // // Alt+shift+number.
        // "\u001b[1;4A" -> (Key.Escape, Key.shiftUp),
        // "\u001b[1;4B" -> (Key.Escape, Key.shiftDown),
        // "\u001b[1;4C" -> (Key.Escape, Key.shiftRight),
        // "\u001b[1;4D" -> (Key.Escape, Key.shiftLeft),
        // "\u001b[1;4F" -> (Key.Escape, Key.shiftEnd),
        // "\u001b[1;4H" -> (Key.Escape, Key.shiftHome),
        // Control + arrows.
        "\u001b[1;5A" -> (Key.controlUp), // Cursor Mode
        "\u001b[1;5B" -> (Key.controlDown), // Cursor Mode
        "\u001b[1;5C" -> (Key.controlRight), // Cursor Mode
        "\u001b[1;5D" -> (Key.controlLeft), // Cursor Mode
        "\u001bf" -> (Key.controlRight), // iTerm natural editing keys
        "\u001bb" -> (Key.controlLeft), // iTerm natural editing keys
        "\u001b[1;5F" -> (Key.controlEnd),
        "\u001b[1;5H" -> (Key.controlHome),
        // rxvt
        "\u001b[7^" -> (Key.controlEnd),
        "\u001b[8^" -> (Key.controlHome),
        // Tmux sends following keystrokes when control+arrow is pressed, but for
        // Emacs ansi-term sends the same sequences for normal arrow keys. Consider
        // it a normal arrow press, because that's more important.
        "\u001b[5A" -> (Key.controlUp),
        "\u001b[5B" -> (Key.controlDown),
        "\u001b[5C" -> (Key.controlRight),
        "\u001b[5D" -> (Key.controlLeft),
        // Control arrow keys in rxvt
        "\u001bOa" -> (Key.controlUp),
        "\u001bOb" -> (Key.controlUp),
        "\u001bOc" -> (Key.controlRight),
        "\u001bOd" -> (Key.controlLeft),
        // Control + shift + arrows.
        "\u001b[1;6A" -> (Key.controlShiftUp),
        "\u001b[1;6B" -> (Key.controlShiftDown),
        "\u001b[1;6C" -> (Key.controlShiftRight),
        "\u001b[1;6D" -> (Key.controlShiftLeft),
        "\u001b[1;6F" -> (Key.controlShiftEnd),
        "\u001b[1;6H" -> (Key.controlShiftHome),
        // Control + Meta + arrows.
        // "\u001b[1;7A" -> (Key.Escape, Key.controlUp),
        // "\u001b[1;7B" -> (Key.Escape, Key.controlDown),
        // "\u001b[1;7C" -> (Key.Escape, Key.controlRight),
        // "\u001b[1;7D" -> (Key.Escape, Key.controlLeft),
        // "\u001b[1;7F" -> (Key.Escape, Key.controlEnd),
        // "\u001b[1;7H" -> (Key.Escape, Key.controlHome),
        // Meta + Shift + arrows.
        // "\u001b[1;8A" -> (Key.Escape, Key.controlShiftUp),
        // "\u001b[1;8B" -> (Key.Escape, Key.controlShiftDown),
        // "\u001b[1;8C" -> (Key.Escape, Key.controlShiftRight),
        // "\u001b[1;8D" -> (Key.Escape, Key.controlShiftLeft),
        // "\u001b[1;8F" -> (Key.Escape, Key.controlShiftEnd),
        // "\u001b[1;8H" -> (Key.Escape, Key.controlShiftHome),
        // Meta + arrow on (some?) Macs when using iTerm defaults (see issue //483).
        // "\u001b[1;9A" -> (Key.Escape, Key.up),
        // "\u001b[1;9B" -> (Key.Escape, Key.down),
        // "\u001b[1;9C" -> (Key.Escape, Key.right),
        // "\u001b[1;9D" -> (Key.Escape, Key.left),
        // --
        // Control/shift/meta + number in mintty.
        // (c-2 will actually send c-@ and c-6 will send c-^.)
        // "\u001b[1;5p" -> (Key.control0),
        // "\u001b[1;5q" -> (Key.control1),
        // "\u001b[1;5r" -> (Key.control2),
        // "\u001b[1;5s" -> (Key.control3),
        // "\u001b[1;5t" -> (Key.control4),
        // "\u001b[1;5u" -> (Key.control5),
        // "\u001b[1;5v" -> (Key.control6),
        // "\u001b[1;5w" -> (Key.control7),
        // "\u001b[1;5x" -> (Key.control8),
        // "\u001b[1;5y" -> (Key.control9),
        // "\u001b[1;6p" -> (Key.controlShift0),
        // "\u001b[1;6q" -> (Key.controlShift1),
        // "\u001b[1;6r" -> (Key.controlShift2),
        // "\u001b[1;6s" -> (Key.controlShift3),
        // "\u001b[1;6t" -> (Key.controlShift4),
        // "\u001b[1;6u" -> (Key.controlShift5),
        // "\u001b[1;6v" -> (Key.controlShift6),
        // "\u001b[1;6w" -> (Key.controlShift7),
        // "\u001b[1;6x" -> (Key.controlShift8),
        // "\u001b[1;6y" -> (Key.controlShift9),
        // "\u001b[1;7p" -> (Key.Escape, Key.control),
        // "\u001b[1;7q" -> (Key.Escape, Key.control),
        // "\u001b[1;7r" -> (Key.Escape, Key.control),
        // "\u001b[1;7s" -> (Key.Escape, Key.control),
        // "\u001b[1;7t" -> (Key.Escape, Key.control),
        // "\u001b[1;7u" -> (Key.Escape, Key.control),
        // "\u001b[1;7v" -> (Key.Escape, Key.control),
        // "\u001b[1;7w" -> (Key.Escape, Key.control),
        // "\u001b[1;7x" -> (Key.Escape, Key.control),
        // "\u001b[1;7y" -> (Key.Escape, Key.control),
        // "\u001b[1;8p" -> (Key.Escape, Key.controlShift1),
        // "\u001b[1;8q" -> (Key.Escape, Key.controlShift2),
        // "\u001b[1;8r" -> (Key.Escape, Key.controlShift3),
        // "\u001b[1;8s" -> (Key.Escape, Key.controlShift4),
        // "\u001b[1;8t" -> (Key.Escape, Key.controlShift5),
        // "\u001b[1;8u" -> (Key.Escape, Key.controlShift6),
        // "\u001b[1;8v" -> (Key.Escape, Key.controlShift7),
        // "\u001b[1;8w" -> (Key.Escape, Key.controlShift8),
        // "\u001b[1;8x" -> (Key.Escape, Key.controlShift9),
        // "\u001b[1;8y" -> (Key.Escape, Key.controlShift0),
        // Simplify some sequences that appear to be unique to rxvt; see
        // https ->/github.com/Textualize/textual/issues/3741 for context.
        "\u001bOj" -> Key('*'),
        "\u001bOk" -> Key('+'),
        "\u001bOm" -> Key('-'),
        "\u001bOn" -> Key('.'),
        "\u001bOo" -> Key('/'),
        "\u001bOp" -> Key('0'),
        "\u001bOq" -> Key('1'),
        "\u001bOr" -> Key('2'),
        "\u001bOs" -> Key('3'),
        "\u001bOt" -> Key('4'),
        "\u001bOu" -> Key('5'),
        "\u001bOv" -> Key('6'),
        "\u001bOw" -> Key('7'),
        "\u001bOx" -> Key('8'),
        "\u001bOy" -> Key('9'),
        "\u001bOM" -> (Key.enter),
        // WezTerm on macOS emits sequences for Opt and keys on the top numeric
        // row; whereas other terminals provide various characters. The following
        // swallow up those sequences and turns them into characters the same as
        // the other terminals.
        "\u001b§" -> Key('§'),
        "\u001b1" -> Key('¡'),
        "\u001b2" -> Key('™'),
        "\u001b3" -> Key('£'),
        "\u001b4" -> Key('¢'),
        "\u001b5" -> Key('∞'),
        "\u001b6" -> Key('§'),
        "\u001b7" -> Key('¶'),
        "\u001b8" -> Key('•'),
        "\u001b9" -> Key('ª'),
        "\u001b0" -> Key('º'),
        "\u001b-" -> Key('–'),
        "\u001b=" -> Key('≠'),
        // Ctrl+§ on kitty is different from most other terminals on macOS.
        "\u001b[167;5u" -> Key('0')
      )

    codes.foreach { (input, key) =>
      assertEquals(StringBufferReader(input).readKey(), key)
    }
  }
}
