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

package terminus.example

import terminus.*
import terminus.effect

import scala.annotation.tailrec

class Prompt[
    Terminal <: effect.Color[Terminal] & effect.Cursor &
      effect.Format[Terminal] & effect.Erase & effect.KeyReader & effect.Writer
](terminal: Color & Cursor & Format & Erase & KeyReader & Writer) {

  type Program[A] = Terminal ?=> A

  type PromptKey = KeyCode.Enter.type | KeyCode.Up.type | KeyCode.Down.type

  // Clear the text we've written
  def clear(): Program[Unit] = {
    terminal.cursor.move(1, -4)
    terminal.erase.down()
    terminal.cursor.column(1)
  }

  // Write an option the user can choose. The currently selected option is highlighted.
  def writeChoice(description: String, selected: Boolean): Program[Unit] =
    if selected then
      terminal.format.bold(terminal.write(s"> ${description}\r\n"))
    else terminal.write(s"  ${description}\r\n")

  // Write the UI
  def write(selected: Int): Program[Unit] = {
    terminal.write("How cool is this?\r\n")
    writeChoice("Very cool", selected == 0)
    writeChoice("Way cool", selected == 1)
    writeChoice("So cool", selected == 2)
    terminal.flush()
  }

  @tailrec
  final def read(): Program[PromptKey] = {
    terminal.readKey() match {
      case Eof =>
        throw new Exception("Received an EOF")
      case key: Key =>
        key match {
          case Key(_, KeyCode.Enter) => KeyCode.Enter
          case Key(_, KeyCode.Up)    => KeyCode.Up
          case Key(_, KeyCode.Down)  => KeyCode.Down
          case other                 => read()
        }
    }
  }

  def loop(idx: Int): Program[Int] = {
    write(idx)
    read() match {
      case KeyCode.Up =>
        clear()
        loop(if idx == 0 then 2 else idx - 1)

      case KeyCode.Down =>
        clear()
        loop(if idx == 2 then 0 else idx + 1)

      case KeyCode.Enter => idx
    }
  }
}
