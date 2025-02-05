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
}
type Program[A] = Terminal ?=> A

object Terminal extends Alert, Color, Cursor, Format, Erase, Writer {
  def readKey(): Program[Future[String]] =
    terminal ?=> terminal.readKey()

  def run[A](id: String, rows: Int = 24, cols: Int = 80)(f: Program[A]): A = {
    val options = XtermJsOptions(rows, cols)
    run(dom.document.getElementById(id).asInstanceOf[HTMLElement], options)(f)
  }

  def run[A](element: HTMLElement, options: XtermJsOptions)(
      f: Program[A]
  ): A = {
    val terminal = Terminal(element, options)
    f(using terminal)
  }
}
