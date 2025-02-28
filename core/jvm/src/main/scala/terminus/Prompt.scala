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

import terminus.effect.Eof
import terminus.effect.Key

import scala.annotation.tailrec

// The only keys we care about
enum KeyCode {
  case Enter
  case Up
  case Down
}

// Clear the text we've written
def clear(): Program[Unit] = {
  Terminal.cursor.move(1, -4)
  Terminal.erase.down()
  Terminal.cursor.column(1)
}

// Write an option the user can choose. The currently selected option is highlighted.
def writeChoice(description: String, selected: Boolean): Program[Unit] =
  if selected then Terminal.format.bold(Terminal.write(s"> ${description}\r\n"))
  else Terminal.write(s"  ${description}\r\n")

// Write the UI
def write(selected: Int): Program[Unit] = {
  Terminal.write("How cool is this?\r\n")
  writeChoice("Very cool", selected == 0)
  writeChoice("Way cool", selected == 1)
  writeChoice("So cool", selected == 2)
  Terminal.flush()
}

@tailrec
def read(): Program[KeyCode] = {
  Terminal.readKey() match {
    case Eof =>
      throw new Exception("Received an EOF")
    case key: Key =>
      key match {
        case Key.Enter => KeyCode.Enter
        case Key.Up    => KeyCode.Up
        case Key.Down  => KeyCode.Down
        case other     => read()
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

@main def prompt(): Unit = {
  val idx =
    Terminal.run(
      Terminal.raw { loop(0) }
    )

  println(s"Selected $idx")
}
