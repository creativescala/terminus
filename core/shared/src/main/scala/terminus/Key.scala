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

/** Enumeration describing the key presses we recognize from the terminal */
final case class Key(modifiers: KeyModifier, code: KeyCode)
object Key {

  /** Construct a Key from an unmodified Char */
  def apply(char: Char): Key =
    Key(KeyModifier.None, KeyCode.Character(char))

  /** Construct a Key from an unmodified KeyCode */
  def apply(code: KeyCode): Key =
    Key(KeyModifier.None, code)

  /** Construct a Key with a Control modifier */
  def control(char: Char): Key =
    Key(KeyModifier.Control, KeyCode.Character(char))

  /** Construct a Key with a Control modifier */
  def control(code: KeyCode): Key =
    Key(KeyModifier.Control, code)

  /** Construct a Key with a Shift modifier */
  def shift(char: Char): Key =
    Key(KeyModifier.Shift, KeyCode.Character(char))

  /** Construct a Key with a Shift modifier */
  def shift(code: KeyCode): Key =
    Key(KeyModifier.Shift, code)
}
