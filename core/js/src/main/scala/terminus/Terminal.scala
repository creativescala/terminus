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
import org.scalajs.dom.document

class Terminal(root: HTMLElement, options: XtermJsOptions)
    extends effect.Color[Terminal],
      effect.Cursor,
      effect.Display[Terminal],
      effect.Erase,
      effect.Writer {

  private val terminal = new XtermJsTerminal(options)
  terminal.open(root)

  def flush(): Unit = ()

  def write(string: String): Unit =
    terminal.write(string)

  def write(char: Char): Unit =
    terminal.write(char.toString())
}
type Program[A] = Terminal ?=> A

object Terminal extends Color, Cursor, Display, Erase, Writer {
  def run[A](id: String, rows: Int = 24, cols: Int = 80)(f: Program[A]): A = {
    val options = XtermJsOptions(rows, cols)
    run(document.getElementById(id).asInstanceOf[HTMLElement], options)(f)
  }

  def run[A](element: HTMLElement, options: XtermJsOptions)(
      f: Program[A]
  ): A = {
    val terminal = Terminal(element, options)
    f(using terminal)
  }
}
