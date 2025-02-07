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
enum Key {

  /** A standard character, like 'A' or 'z.' */
  case Character(char: Char)
  case Space
  case Escape
  case ShiftEscape
  case Return
  case ControlAt
  case ControlA
  case ControlB
  case ControlC
  case ControlD
  case ControlE
  case ControlF
  case ControlG
  case ControlH
  case ControlI
  case ControlJ
  case ControlK
  case ControlL
  case ControlM
  case ControlN
  case ControlO
  case ControlP
  case ControlQ
  case ControlR
  case ControlS
  case ControlT
  case ControlU
  case ControlV
  case ControlW
  case ControlX
  case ControlY
  case ControlZ
  case Control1
  case Control2
  case Control3
  case Control4
  case Control5
  case Control6
  case Control7
  case Control8
  case Control9
  case Control0
  case ControlShift1
  case ControlShift2
  case ControlShift3
  case ControlShift4
  case ControlShift5
  case ControlShift6
  case ControlShift7
  case ControlShift8
  case ControlShift9
  case ControlShift0
  case ControlBackslash
  case ControlSquareClose
  case ControlCircumflex
  case ControlUnderscore
  case Left
  case Right
  case Up
  case Down
  case Home
  case End
  case Insert
  case Delete
  case PageUp
  case PageDown
  case ControlLeft
  case ControlRight
  case ControlUp
  case ControlDown
  case ControlHome
  case ControlEnd
  case ControlInsert
  case ControlDelete
  case ControlPageUp
  case ControlPageDown
  case ShiftLeft
  case ShiftRight
  case ShiftUp
  case ShiftDown
  case ShiftHome
  case ShiftEnd
  case ShiftInsert
  case ShiftDelete
  case ShiftPageUp
  case ShiftPageDown
  case ControlShiftLeft
  case ControlShiftRight
  case ControlShiftUp
  case ControlShiftDown
  case ControlShiftHome
  case ControlShiftEnd
  case ControlShiftInsert
  case ControlShiftDelete
  case ControlShiftPageUp
  case ControlShiftPageDown
  case BackTab
  case Backspace
  case Enter
  case Tab
  case F1
  case F2
  case F3
  case F4
  case F5
  case F6
  case F7
  case F8
  case F9
  case F10
  case F11
  case F12
  case F13
  case F14
  case F15
  case F16
  case F17
  case F18
  case F19
  case F20
  case F21
  case F22
  case F23
  case F24
  case ControlF1
  case ControlF2
  case ControlF3
  case ControlF4
  case ControlF5
  case ControlF6
  case ControlF7
  case ControlF8
  case ControlF9
  case ControlF10
  case ControlF11
  case ControlF12
  case ControlF13
  case ControlF14
  case ControlF15
  case ControlF16
  case ControlF17
  case ControlF18
  case ControlF19
  case ControlF20
  case ControlF21
  case ControlF22
  case ControlF23
  case ControlF24
  case ScrollUp
  case ScrollDown
}
