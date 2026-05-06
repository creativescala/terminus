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

package terminus.ui.component

import terminus.ui.AppContent
import terminus.ui.AppContext
import terminus.ui.Buffer
import terminus.ui.ChildContext
import terminus.ui.Component
import terminus.ui.Rect
import terminus.ui.Size
import terminus.ui.style.Align
import terminus.ui.style.Justify
import terminus.ui.style.LayoutStyle

import scala.collection.mutable

class Row(val size: Size, val layoutStyle: LayoutStyle = LayoutStyle.default)
    extends ChildContext,
      Component:
  private val children: mutable.ArrayBuffer[Component] =
    mutable.ArrayBuffer.empty

  def add(component: Component): Unit =
    children += component

  def render(bounds: Rect, buf: Buffer): Unit =
    val widths =
      Layout.resolveAxis(
        children.view.map(_.size.width).toIndexedSeq,
        bounds.width
      )
    val freeSpace = (bounds.width - widths.sum).max(0)
    val n = children.size

    val (startOffset, gap) = layoutStyle.justify match
      case Justify.Start        => (0, 0)
      case Justify.End          => (freeSpace, 0)
      case Justify.Center       => (freeSpace / 2, 0)
      case Justify.SpaceBetween => (0, if n > 1 then freeSpace / (n - 1) else 0)
      case Justify.SpaceAround  =>
        val space = if n > 0 then freeSpace / n else 0
        (space / 2, space)
      case Justify.SpaceEvenly =>
        val space = if n > 0 then freeSpace / (n + 1) else 0
        (space, space)

    var x = bounds.x + startOffset
    children.zip(widths).foreach { (child, w) =>
      val naturalH = Layout.resolveSingle(child.size.height, bounds.height)
      val (y, h) = layoutStyle.align match
        case Align.Stretch => (bounds.y, bounds.height)
        case Align.Start   => (bounds.y, naturalH)
        case Align.End     => (bounds.y + bounds.height - naturalH, naturalH)
        case Align.Center  =>
          (bounds.y + (bounds.height - naturalH) / 2, naturalH)
      child.render(Rect(x, y, w, h), buf)
      x += w + gap
    }

object Row:
  def apply[A](
      size: Size,
      style: LayoutStyle => LayoutStyle = identity
  )(f: AppContent[A])(using
      parent: AppContext
  ): A =
    val row = new Row(size, style(LayoutStyle.default))
    given AppContext = AppContext.child(parent, row)
    val result = f
    parent.add(row)
    result
