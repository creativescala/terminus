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

import org.scalajs.dom
import org.scalajs.dom.HTMLElement

import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.Promise

class Terminal(root: HTMLElement, options: XtermJsOptions)
    extends effect.Color[Terminal],
      effect.Cursor,
      effect.Format[Terminal],
      effect.Erase,
      effect.Dimensions, // Add Dimensions trait
      effect.Writer {

  private val keyBuffer: mutable.ArrayDeque[Promise[String]] =
    new mutable.ArrayDeque[Promise[String]](8)

  private val terminal = new XtermJsTerminal(options)
  terminal.open(root)
  terminal.onKey { (event: XtermKeyEvent) =>
    keyBuffer.removeHead().success(event.domEvent.key)
    ()
  }

  /** Block reading a Javascript keycode */
  def readKey(): Future[String] = {
    val promise = Promise[String]()
    keyBuffer.append(promise)

    promise.future
  }

  def flush(): Unit = ()

  def write(string: String): Unit =
    terminal.write(string)
  def write(char: Char): Unit =
    terminal.write(char.toString())

  // Dimensions implementation
  def getDimensions: effect.TerminalDimensions =
    effect.TerminalDimensions(terminal.cols, terminal.rows)

  def setDimensions(dimensions: effect.TerminalDimensions): Unit =
    terminal.resize(dimensions.columns, dimensions.rows)
}
type Program[A] = Terminal ?=> A

object Terminal extends Color, Cursor, Format, Erase, Writer {
  def readKey(): Program[Future[String]] =
    terminal ?=> terminal.readKey()

  object dimensions {
    def get: Program[effect.TerminalDimensions] =
      terminal ?=> terminal.getDimensions

    def set(dimensions: effect.TerminalDimensions): Program[Unit] =
      terminal ?=> terminal.setDimensions(dimensions)
  }
  def run[A](id: String, cols: Int = 80, rows: Int = 24)(f: Program[A]): A = {
    val options = XtermJsOptions(cols, rows) // Ensure XtermJsOptions is called with cols, then rows
    run(dom.document.getElementById(id).asInstanceOf[HTMLElement], options)(f)
  }

  def run[A](element: HTMLElement, options: XtermJsOptions)(
      f: Program[A]
  ): A = {
    val terminal = Terminal(element, options)
    f(using terminal)
  }
}
