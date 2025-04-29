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

import munit.FunSuite
import cats.Show
import cats.syntax.show.*

class KeyShowSuite extends FunSuite {
  test("Show[Key] produces the expected string representation") {
    val testCases = Map[Key, String](
      // Basic character keys
      Key('a') -> "a",
      Key('Z') -> "Z",
      Key('1') -> "1",
      Key(' ') -> "Space",
      Key('\t') -> "Tab",
      Key('\n') -> "Newline",

      // Basic special keys
      Key.enter -> "Enter",
      Key.escape -> "Escape",
      Key.backspace -> "Backspace",
      Key.tab -> "Tab",
      Key.up -> "Up",
      Key.down -> "Down",
      Key.left -> "Left",
      Key.right -> "Right",

      // Function keys
      Key.f1 -> "F1",
      Key.f12 -> "F12",

      // Single modifier keys
      Key.shift('a') -> "Shift-a",
      Key.control('c') -> "Control-c",
      Key(KeyModifier.Alt, KeyCode.Character('x')) -> "Alt-x",
      Key(KeyModifier.Super, KeyCode.Character('w')) -> "Super-w",
      Key(KeyModifier.Shift, KeyCode.Character(' ')) -> "Shift-Space",
      Key(KeyModifier.Control, KeyCode.Character('\n')) -> "Control-Newline",

      // Modifier + special keys
      Key.shiftUp -> "Shift-Up",
      Key.controlDown -> "Control-Down",
      Key.controlF12 -> "Control-F12",

      // Multiple modifiers
      Key(
        KeyModifier.ControlShift,
        KeyCode.Character('a')
      ) -> "Control-Shift-a",
      Key.controlShiftUp -> "Control-Shift-Up",
      Key(
        KeyModifier.Control.or(KeyModifier.Alt),
        KeyCode.F(5)
      ) -> "Control-Alt-F5",

      // Complex modifier combinations
      Key(
        KeyModifier.Control.or(KeyModifier.Shift).or(KeyModifier.Alt),
        KeyCode.F(4)
      ) -> "Control-Shift-Alt-F4",
      Key(
        KeyModifier.Control.or(KeyModifier.Shift).or(KeyModifier.Super),
        KeyCode.Enter
      ) -> "Control-Shift-Super-Enter",

      // Unknown key code
      Key.unknown("test") -> "Unknown(test)"
    )

    testCases.foreach { case (key, expected) =>
      val actual = key.show
      assertEquals(
        actual,
        expected,
        s"Key $key did not produce the expected string representation"
      )
    }
  }
}
