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

/** The codes for controlling terminal functionality, commonly known as ANSI
  * escape codes. See:
  *
  *   - https://en.wikipedia.org/wiki/ANSI_escape_code
  *   - https://www.man7.org/linux/man-pages/man4/console_codes.4.html
  *   - https://invisible-island.net/xterm/ctlseqs/ctlseqs.html
  *   - https://ghostty.org/docs/vt
  */
object AnsiCodes {

  /** The escape character, which all codes begin with. */
  val esc: Char = '\u001b'

  /** The Control Sequencer Introducer code, which starts many escape codes. It
    * is ESC[
    */
  val csiCode: String = s"${esc}["

  /** Create a CSI escape code. The terminator must be specifed first, followed
    * by zero or more arguments. The arguments will printed semi-colon separated
    * before the terminator.
    */
  def csi(terminator: String, args: String*): String = {
    s"$csiCode${args.mkString(";")}$terminator"
  }

  /** Create a Select Graphic Rendition code, which is a form of CSI code that
    * controls graphics effects.
    */
  def sgr(n: String): String =
    csi("m", n)

  /** Codes for manipulating the cursor */
  object cursor {

    /** Restore the previously saved cursor state, or reset it to default values
      * if no state has been saved.
      */
    val restore: String =
      s"${esc}8"

    /** Save the cursor location, character set, pending wrap state, SGR
      * attributes, and origin mode.
      */
    val save: String =
      s"${esc}7"

    object style {
      val default = s"${esc}0 q"

      object block {
        val blink = s"${esc}1 q"
        val steady = s"${esc}2 q"
      }

      object underline {
        val blink = s"${esc}3 q"
        val steady = s"${esc}4 q"
      }

      object bar {
        val blink = s"${esc}5 q"
        val steady = s"${esc}6 q"
      }
    }

    object down {

      /** Move down the given number of lines and to the beginning of the line.
        */
      def line(n: Int): String =
        csi("E", n.toString)

      /** Move down the given number of lines. */
      def apply(n: Int): String =
        csi("B", n.toString)
    }

    object up {

      /** Move up the given number of lines and to the beginning of the line.
        */
      def line(n: Int): String =
        csi("F", n.toString)

      /** Move up the given number of lines. */
      def apply(n: Int): String =
        csi("A", n.toString)
    }

    /** Move the given number of characters to the right. */
    def forward(n: Int): String =
      csi("C", n.toString)

    /** Move the given number of characters to the left. */
    def backward(n: Int): String =
      csi("D", n.toString)

    /** Move the cursor to the given column. The left-most column is 1, and
      * coordinates increase to the right.
      */
    def column(n: Int): String =
      csi("G", n.toString)

    /** Move the cursor to the given position, where (1, 1) is the top left
      * corner and coordinates increase to the right and down.
      */
    def to(x: Int, y: Int): String =
      csi("H", y.toString, x.toString)
  }

  object display {
    object bold {
      val on: String = sgr("1")
      val off: String = sgr("22")
    }
    object light {
      val on: String = sgr("2")
      val off: String = sgr("22")
    }

    /** The support for underlines follows the Kitty extension defined at
      * https://sw.kovidgoyal.net/kitty/underlines/
      */
    object underline {
      val reset: String = csi("m", "4", "0")
      val straight: String = csi("m", "4", "1")
      val double: String = csi("m", "4", "2")
      val curly: String = csi("m", "4", "3")
      val dotted: String = csi("m", "4", "4")
      val dashed: String = csi("m", "4", "5")

      // Not sure this will work.
      val default: String = sgr("59")
      val black: String = sgr("50")
      val red: String = sgr("51")
      val green: String = sgr("52")
      val yellow: String = sgr("53")
      val blue: String = sgr("54")
      val magenta: String = sgr("55")
      val cyan: String = sgr("56")
      val white: String = sgr("57")
    }

    object blink {
      val on: String = sgr("5")
      val off: String = sgr("25")
    }

    object invert {
      val on: String = sgr("7")
      val off: String = sgr("27")
    }

    object invisible {
      val on: String = sgr("8")
      val off: String = sgr("28")
    }

    object strikethrough {
      val on: String = sgr("9")
      val off: String = sgr("29")
    }
  }

  object erase {

    /** Erase the entire screen and move the cursor to the top-left. */
    val screen: String = csi("J", "2")

    /** Erase from current cursor position to the end of the screen. */
    val down: String = csi("J", "0")

    /** Erase from current cursor position to the start of the screen. */
    val up: String = csi("J", "1")

    /** Erase the current line. */
    val line: String = csi("K", "2")
  }

  object mode {

    /** Application mode, which seems to only change the codes sent by some
      * keys. See https://www.vt100.net/docs/vt510-rm/DECCKM
      */
    object application {
      val on: String = csi("?1h")
      val off: String = csi("?1l")
    }

    /** Alternate screen mode, which means content will not be shown when the
      * program exits and key presses will not be saved to the history buffer.
      * See https://invisible-island.net/xterm/ctlseqs/ctlseqs.html and search
      * for "alternate screen"
      */
    object alternateScreen {
      val on: String = csi("?1049h")
      val off: String = csi("?11049l")
    }
  }

  object scroll {

    /** Scroll the display up the given number of rows. Defaults to 1 row. */
    def up(lines: Int = 1): String =
      csi("S", lines.toString)

    /** Scroll the display down the given number of rows. Defaults to 1 row. */
    def down(lines: Int = 1): String =
      csi("T", lines.toString)
  }

  /** Set foreground color. */
  object foreground {
    val default: String = sgr("39")
    val black: String = sgr("30")
    val red: String = sgr("31")
    val green: String = sgr("32")
    val yellow: String = sgr("33")
    val blue: String = sgr("34")
    val magenta: String = sgr("35")
    val cyan: String = sgr("36")
    val white: String = sgr("37")
    val brightBlack: String = sgr("90")
    val brightRed: String = sgr("91")
    val brightGreen: String = sgr("92")
    val brightYellow: String = sgr("93")
    val brightBlue: String = sgr("94")
    val brightMagenta: String = sgr("95")
    val brightCyan: String = sgr("96")
    val brightWhite: String = sgr("97")
  }

  /** Set background color. */
  object background {
    val default: String = sgr("49")
    val black: String = sgr("40")
    val red: String = sgr("41")
    val green: String = sgr("42")
    val yellow: String = sgr("43")
    val blue: String = sgr("44")
    val magenta: String = sgr("45")
    val cyan: String = sgr("46")
    val white: String = sgr("47")
    val brightBlack: String = sgr("100")
    val brightRed: String = sgr("101")
    val brightGreen: String = sgr("102")
    val brightYellow: String = sgr("103")
    val brightBlue: String = sgr("104")
    val brightMagenta: String = sgr("105")
    val brightCyan: String = sgr("106")
    val brightWhite: String = sgr("107")
  }
}
