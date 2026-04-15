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

package terminus.ui

import terminus.AlternateScreenMode
import terminus.Cursor
import terminus.Erase
import terminus.Writer
import terminus.effect

import scala.collection.mutable

/** A RootContext that acts as a column and renders into a cell buffer. */
class FullScreen() extends RootContext:
  private val children: mutable.ArrayBuffer[Component] =
    mutable.ArrayBuffer.empty

  private var childrenSize: Size = Size.zero

  def size: Size = childrenSize

  def add(component: Component): Unit =
    childrenSize = childrenSize.column(component.size)
    children += component

  def render(using Terminal): Unit =
    val buf = Buffer(childrenSize.width, childrenSize.height)
    var y = 0
    children.foreach { child =>
      child.render(Rect(0, y, child.size.width, child.size.height), buf)
      y += child.size.height
    }
    buf.render

object FullScreen:
  type Terminal = effect.AlternateScreenMode & effect.Erase &
    terminus.ui.Terminal
  type Program[A] = FullScreen.Terminal ?=> A

  object Terminal extends AlternateScreenMode, Cursor, Erase, Writer

  def apply[A](f: RenderContext ?=> A): FullScreen.Program[A] =
    val fullScreen = new FullScreen()
    val result = f(using fullScreen)
    FullScreen.Terminal.erase.screen()
    fullScreen.render
    result
