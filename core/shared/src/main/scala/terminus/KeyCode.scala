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
  case Backspace
  case BackTab
  case Delete
  case Enter
  case Escape
  case Insert
  case Left
  case Right
  case Up
  case Down
  case Home
  case End
  case PageUp
  case PageDown
  case Tab
  case F(value: Byte)
  case Character(char: Char)
  case Null
  case CapsLock
  case ScrollLock
  case NumLock
  case PrintScreen
  case Pause
  case Menu
  case KeypadBegin
}
