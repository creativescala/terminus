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

enum KeyCode {
  case BackTab
  case Backspace
  case CapsLock
  case Character(char: Char)
  case Delete
  case Down
  case End
  case Enter
  case Escape
  case F(value: Byte)
  case Home
  case Insert
  case KeypadBegin
  case Left
  case Menu
  case Null
  case NumLock
  case PageDown
  case PageUp
  case Pause
  case PrintScreen
  case Right
  case ScrollLock
  case Tab
  case Up

  /** This case represents a sequence of codes that we don't know how to
    * interpret. The entire sequence is contained here for debugging purposes.
    */
  case Unknown(code: String)
}
