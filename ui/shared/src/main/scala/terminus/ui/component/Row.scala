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

import terminus.ui.layout.Buffer
import terminus.ui.layout.Component
import terminus.ui.layout.Rect
import terminus.ui.layout.Size
import terminus.ui.style.Align
import terminus.ui.style.Justify
import terminus.ui.style.LayoutStyle
import terminus.ui.event.DefaultEvent
import terminus.ui.layout.DefaultLayout
import terminus.ui.capability.Event
import terminus.ui.capability.Layout
import terminus.ui.event.FocusId

import scala.collection.mutable

final class Row(
    val size: Size,
    layoutStyle: LayoutStyle = LayoutStyle.default,
    context: DefaultEvent & DefaultLayout
) extends Component:

  def render(bounds: Rect, buf: Buffer): Unit =
    val widths =
      Layout.resolveAxis(
        context.components.view.map(_.size.width).toIndexedSeq,
        bounds.width
      )
    val freeSpace = (bounds.width - widths.sum).max(0)
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

    var x = bounds.x + startOffset
    context.components.zip(widths).foreach { (child, w) =>
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
  def apply(
      size: Size,
      style: LayoutStyle => LayoutStyle = identity
  )(body: Event & Layout ?=> Unit)(using
      ctx: Layout
  ): Unit =
    ctx.addComponent { runtime =>
      val focusId = FocusId.next
      val context = new DefaultEvent(focusId, runtime)
        with DefaultLayout(runtime) {}
      // Evaluate body here so we do not retain a reference to it and it can be garbage collected.
      body(using context)

      new Row(size, style(LayoutStyle.default), context)
    }
