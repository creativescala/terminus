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

import org.scalajs.dom.HTMLElement

class Terminal(root: HTMLElement)
    extends effect.Color[Terminal],
      effect.Cursor,
      effect.Display[Terminal],
      effect.Erase,
      effect.Writer {

  private val terminal = new XtermJsTerminal()
  terminal.open(root)

  def flush(): Unit = ()

  def write(string: String): Unit =
    terminal.write(string)

  def write(char: Char): Unit =
    terminal.write(char.toString())
}
type Program[A] = Terminal ?=> A

object Terminal extends Color, Cursor, Display, Erase, Writer {}
