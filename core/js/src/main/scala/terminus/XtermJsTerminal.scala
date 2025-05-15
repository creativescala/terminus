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
import org.scalajs.dom.KeyboardEvent

import scala.annotation.unused
import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

@js.native
trait XtermKeyEvent extends js.Object {
  val key: String = js.native
  val domEvent: KeyboardEvent = js.native
}

@js.native
@JSGlobal("Terminal")
/** Minimal definition of the Terminal type from xterm.js */
class XtermJsTerminal(@unused options: XtermJsOptions) extends js.Object {
  val onKey: js.Function1[js.Function1[XtermKeyEvent, Unit], Unit] = js.native
  def open(element: dom.HTMLElement): Unit = js.native
  def write(data: String): Unit = js.native

  // Dimensions methods
  def cols: Int = js.native
  def rows: Int = js.native
  def resize(cols: Int, rows: Int): Unit = js.native
}
