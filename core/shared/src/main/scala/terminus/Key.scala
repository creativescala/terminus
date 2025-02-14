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

/** Represents a key press. The information is split between the key code and
  * any modifiers that were pressed. We only interpret modifiers if the terminal
  * passes that information to us. So, for example, 'A' is not represented as
  * Shift + 'a' as the terminal does not send that information.
  */
final case class Key(modifiers: KeyModifier, code: KeyCode)
object Key {

  /** Construct a Key from an unmodified Char */
  def apply(char: Char): Key =
    Key(KeyModifier.None, KeyCode.Character(char))

  /** Construct a Key from an unmodified KeyCode */
  def apply(code: KeyCode): Key =
    Key(KeyModifier.None, code)

  /** Construct a Key with a val controlmodifier */
  def control(char: Char): Key =
    Key(KeyModifier.Control, KeyCode.Character(char))

  /** Construct a Key with a val controlmodifier */
  def control(code: KeyCode): Key =
    Key(KeyModifier.Control, code)

  /** Construct a Key with a Shift modifier */
  def shift(char: Char): Key =
    Key(KeyModifier.Shift, KeyCode.Character(char))

  /** Construct a Key with a Shift modifier */
  def shift(code: KeyCode): Key =
    Key(KeyModifier.Shift, code)

  /** Construct a Key representing an unknown sequence of escape codes from the
    * terminal.
    */
  def unknown(code: String): Key =
    Key(KeyModifier.None, KeyCode.Unknown(code))

  // Predfined keys for ease of use

  val controlAt: Key = Key.control('@')
  val controlA = Key.control('a')
  val controlB = Key.control('b')
  val controlC = Key.control('c')
  val controlD = Key.control('d')
  val controlE = Key.control('e')
  val controlF = Key.control('f')
  val controlG = Key.control('g')
  val controlK = Key.control('k')
  val controlL = Key.control('l')
  val controlN = Key.control('n')
  val controlO = Key.control('o')
  val controlP = Key.control('p')
  val controlQ = Key.control('q')
  val controlR = Key.control('r')
  val controlS = Key.control('s')
  val controlT = Key.control('t')
  val controlU = Key.control('u')
  val controlV = Key.control('v')
  val controlW = Key.control('w')
  val controlX = Key.control('x')
  val controlY = Key.control('y')
  val controlZ = Key.control('z')

  val backspace = Key(KeyCode.Backspace)
  val space = Key(' ')
  val tab = Key('\t')
  val backTab = Key(KeyCode.BackTab)
  val newLine = Key('\n')

  /** An alias for `newLine`. */
  val lineFeed = newLine
  val enter = Key(KeyCode.Enter)

  /** An alias for `enter`. */
  val carriageReturn = enter
  val escape = Key(KeyCode.Escape)

  val up = Key(KeyCode.Up)
  val down = Key(KeyCode.Down)
  val right = Key(KeyCode.Right)
  val left = Key(KeyCode.Left)

  val home = Key(KeyCode.Home)
  val `end` = Key(KeyCode.End)
  val insert = Key(KeyCode.Insert)
  val delete = Key(KeyCode.Delete)
  val pageUp = Key(KeyCode.PageUp)
  val pageDown = Key(KeyCode.PageDown)

  val f1 = Key(KeyCode.F(1))
  val f2 = Key(KeyCode.F(2))
  val f3 = Key(KeyCode.F(3))
  val f4 = Key(KeyCode.F(4))
  val f5 = Key(KeyCode.F(5))
  val f6 = Key(KeyCode.F(6))
  val f7 = Key(KeyCode.F(7))
  val f8 = Key(KeyCode.F(8))
  val f9 = Key(KeyCode.F(9))
  val f10 = Key(KeyCode.F(10))
  val f11 = Key(KeyCode.F(11))
  val f12 = Key(KeyCode.F(12))
  val f13 = Key(KeyCode.F(13))
  val f14 = Key(KeyCode.F(14))
  val f15 = Key(KeyCode.F(15))
  val f16 = Key(KeyCode.F(16))
  val f17 = Key(KeyCode.F(17))
  val f18 = Key(KeyCode.F(18))
  val f19 = Key(KeyCode.F(19))
  val f20 = Key(KeyCode.F(20))
  val f21 = Key(KeyCode.F(21))
  val f22 = Key(KeyCode.F(22))
  val f23 = Key(KeyCode.F(23))
  val f24 = Key(KeyCode.F(24))

  val shiftUp = Key.shift(KeyCode.Up)
  val shiftDown = Key.shift(KeyCode.Down)
  val shiftRight = Key.shift(KeyCode.Right)
  val shiftLeft = Key.shift(KeyCode.Left)
  val shiftHome = Key.shift(KeyCode.Home)
  val shiftEnd = Key.shift(KeyCode.End)
  val shiftEscape = Key.shift(KeyCode.Escape)
  val shiftInsert = Key.shift(KeyCode.Insert)
  val shiftDelete = Key.shift(KeyCode.Delete)
  val shiftPageUp = Key.shift(KeyCode.PageUp)
  val shiftPageDown = Key.shift(KeyCode.PageDown)

  val controlUp = Key.control(KeyCode.Up)
  val controlDown = Key.control(KeyCode.Down)
  val controlRight = Key.control(KeyCode.Right)
  val controlLeft = Key.control(KeyCode.Left)
  val controlHome = Key.control(KeyCode.Home)
  val controlEnd = Key.control(KeyCode.End)
  val controlBackslash = Key.control(KeyCode.Character('\\'))
  val controlSquareClose = Key.control(KeyCode.Character(']'))
  val controlCircumflex = Key.control(KeyCode.Character('^'))
  val controlUnderscore = Key.control(KeyCode.Character('_'))
  val controlDelete = Key.control(KeyCode.Delete)
  val controlPageUp = Key.control(KeyCode.PageUp)
  val controlPageDown = Key.control(KeyCode.PageDown)
  val controlF1 = Key.control(KeyCode.F(1))
  val controlF2 = Key.control(KeyCode.F(2))
  val controlF3 = Key.control(KeyCode.F(3))
  val controlF4 = Key.control(KeyCode.F(4))
  val controlF5 = Key.control(KeyCode.F(5))
  val controlF6 = Key.control(KeyCode.F(6))
  val controlF7 = Key.control(KeyCode.F(7))
  val controlF8 = Key.control(KeyCode.F(8))
  val controlF9 = Key.control(KeyCode.F(9))
  val controlF10 = Key.control(KeyCode.F(10))
  val controlF11 = Key.control(KeyCode.F(11))
  val controlF12 = Key.control(KeyCode.F(12))
  val controlF13 = Key.control(KeyCode.F(13))
  val controlF14 = Key.control(KeyCode.F(14))
  val controlF15 = Key.control(KeyCode.F(15))
  val controlF16 = Key.control(KeyCode.F(16))
  val controlF17 = Key.control(KeyCode.F(17))
  val controlF18 = Key.control(KeyCode.F(18))
  val controlF19 = Key.control(KeyCode.F(19))
  val controlF20 = Key.control(KeyCode.F(20))
  val controlF21 = Key.control(KeyCode.F(21))
  val controlF22 = Key.control(KeyCode.F(22))
  val controlF23 = Key.control(KeyCode.F(23))
  val controlF24 = Key.control(KeyCode.F(24))

  val controlShiftUp = Key(KeyModifier.ControlShift, KeyCode.Up)
  val controlShiftDown = Key(KeyModifier.ControlShift, KeyCode.Down)
  val controlShiftRight = Key(KeyModifier.ControlShift, KeyCode.Right)
  val controlShiftLeft = Key(KeyModifier.ControlShift, KeyCode.Left)
  val controlShiftHome = Key(KeyModifier.ControlShift, KeyCode.Home)
  val controlShiftEnd = Key(KeyModifier.ControlShift, KeyCode.End)
  val controlShiftDelete = Key(KeyModifier.ControlShift, KeyCode.Delete)
  val controlShiftPageUp = Key(KeyModifier.ControlShift, KeyCode.PageUp)
  val controlShiftPageDown = Key(KeyModifier.ControlShift, KeyCode.PageDown)
}
