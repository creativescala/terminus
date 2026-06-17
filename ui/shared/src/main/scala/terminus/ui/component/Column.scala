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

import terminus.ui.capability.{Event, Layout}
import terminus.ui.layout.Component
import terminus.ui.layout.Rect
import terminus.ui.layout.Size
import terminus.ui.style.Align
import terminus.ui.style.Justify
import terminus.ui.style.LayoutStyle
import terminus.ui.event.DefaultEvent
import terminus.ui.layout.DefaultLayout
import terminus.ui.layout.Buffer
import terminus.ui.event.FocusId

import scala.collection.mutable

final class Column private[ui] (
    size: Size,
    layoutStyle: LayoutStyle = LayoutStyle.default,
    context: DefaultEvent & DefaultLayout
) extends Component:

  def render(bounds: Rect, buf: Buffer): Unit =
    val heights =
      Layout.resolveAxis(
        context.components.view.map(_.size.height).toIndexedSeq,
        bounds.height
      )
    val freeSpace = (bounds.height - heights.sum).max(0)
    val n = context.components.size

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

    var y = bounds.y + startOffset
    context.components.zip(heights).foreach { (child, h) =>
      val naturalW = Layout.resolveSingle(child.size.width, bounds.width)
      val (x, w) = layoutStyle.align match
        case Align.Stretch => (bounds.x, bounds.width)
        case Align.Start   => (bounds.x, naturalW)
        case Align.End     => (bounds.x + bounds.width - naturalW, naturalW)
        case Align.Center  =>
          (bounds.x + (bounds.width - naturalW) / 2, naturalW)
      child.render(Rect(x, y, w, h), buf)
      y += h + gap
    }

object Column:
  def apply(size: Size, style: LayoutStyle => LayoutStyle = identity)(
      body: Event & Layout ?=> Unit
  )(using ctx: Layout): Unit =
    ctx.addComponent { runtime =>
      val focusId = FocusId.next
      val context = new DefaultEvent(focusId, runtime)
        with DefaultLayout(runtime) {}
      // Evaluate body here so we do not retain a reference to it and it can be garbage collected.
      body(using context)

      new Column(size, style(LayoutStyle.default), context)
    }
