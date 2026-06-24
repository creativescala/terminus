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

import terminus.Key
import terminus.ui.capability.Layout
import terminus.ui.capability.React
import terminus.ui.event.DefaultEvent
import terminus.ui.event.FocusId
import terminus.ui.layout.Box
import terminus.ui.layout.Buffer
import terminus.ui.layout.Component
import terminus.ui.layout.Constraint
import terminus.ui.layout.Dimensions
import terminus.ui.layout.Infinity
import terminus.ui.layout.Measurement
import terminus.ui.layout.Rect
import terminus.ui.layout.Size
import terminus.ui.react.Var
import terminus.ui.style.BoxStyle
import terminus.ui.style.CellStyle
import terminus.ui.style.TextStyle
import terminus.ui.text.Line

/** A scrollable selection list component.
  *
  * Registering its key handlers automatically makes it focusable, so it just
  * needs to be placed in the component tree to receive keyboard input. The
  * caller owns `selected` and can read it to react to the user's choice:
  *
  * {{{
  * val choice = Var(0)
  * Select(Size.fixed(30, 8), items, choice)
  * }}}
  *
  * Key bindings: Up/Down move one item, PageUp/PageDown move one page, Home/End
  * jump to first/last item. The viewport scrolls to keep the selected item
  * visible. The selected item is highlighted by inverting the content style;
  * all other items use the content style unchanged.
  */
final class Select[A](
    val size: Size,
    style: TextStyle,
    items: Seq[A],
    selected: Var[Int],
    label: A => String,
    context: DefaultEvent
) extends Component:

  private val scroll = Var(0)

  private def clampSelected(n: Int): Int =
    n.max(0).min((items.length - 1).max(0))

  private def visibleRows: Int =
    size.height match
      case Measurement.Fixed(cells) =>
        (cells - activeBoxStyle.insets.vertical).max(0)
      case _ => 0

  private def scrollToShow(sel: Int): Unit =
    val vr = visibleRows
    val s = scroll.peek
    if sel < s then scroll.set(sel)
    else if sel >= s + vr then scroll.set(sel - vr + 1)

  context.onKey(Key.up) {
    val next = clampSelected(selected.peek - 1)
    selected.set(next)
    scrollToShow(next)
  }
  context.onKey(Key.down) {
    val next = clampSelected(selected.peek + 1)
    selected.set(next)
    scrollToShow(next)
  }
  context.onKey(Key.pageUp) {
    val next = clampSelected(selected.peek - visibleRows)
    selected.set(next)
    scrollToShow(next)
  }
  context.onKey(Key.pageDown) {
    val next = clampSelected(selected.peek + visibleRows)
    selected.set(next)
    scrollToShow(next)
  }
  context.onKey(Key.home) {
    selected.set(0)
    scroll.set(0)
  }
  context.onKey(Key.`end`) {
    val last = clampSelected(Int.MaxValue)
    selected.set(last)
    scrollToShow(last)
  }

  def react(using React): Unit =
    selected.get
    scroll.get
    ()

  def measure(constraint: Constraint): Dimensions =
    val insets = activeBoxStyle.insets
    val inner = insets.deflate(constraint)

    val targetWidth =
      size.width match
        case Measurement.Fixed(cells) => cells - insets.horizontal
        case Measurement.WrapContent  => naturalWidth
        case _                        =>
          inner.maxWidth match
            case Infinity   => naturalWidth
            case bound: Int => bound
    val contentWidth =
      val lowerBounded = targetWidth.max(inner.minWidth)
      inner.maxWidth match
        case Infinity   => lowerBounded
        case bound: Int => lowerBounded.min(bound)

    val contentHeight =
      size.height match
        case Measurement.Fixed(cells) => cells - insets.vertical
        case _                        => items.length

    constraint.constrain(
      insets.inflate(Dimensions(contentWidth, contentHeight))
    )

  def minIntrinsicWidth(height: Int | Infinity): Int =
    naturalWidth + activeBoxStyle.insets.horizontal

  def maxIntrinsicWidth(height: Int | Infinity): Int =
    naturalWidth + activeBoxStyle.insets.horizontal

  def minIntrinsicHeight(width: Int | Infinity): Int =
    items.length + activeBoxStyle.insets.vertical

  def maxIntrinsicHeight(width: Int | Infinity): Int =
    items.length + activeBoxStyle.insets.vertical

  def render(bounds: Rect, buf: Buffer): Unit =
    val ab = activeBoxStyle
    val ac = activeContentStyle

    Box.render(bounds, ab, buf)
    val inner = Box.innerRect(bounds, ab)
    if inner.width <= 0 || inner.height <= 0 then return

    val sel = selected.peek
    val s = scroll.peek

    var row = 0
    while row < inner.height do
      val itemIdx = s + row
      if itemIdx < items.length then
        val raw = label(items(itemIdx))
        val line = Line(raw.take(inner.width).padTo(inner.width, ' '))
        val itemStyle = if itemIdx == sel then ac.withInvert else ac
        buf.putLine(inner.x, inner.y + row, line, itemStyle)
      row += 1

  private def naturalWidth: Int =
    items.map(a => Line(label(a)).width).maxOption.getOrElse(0)

  private def activeBoxStyle: BoxStyle =
    if context.hasFocus then style.focus.map(_.box).getOrElse(style.box)
    else style.box

  private def activeContentStyle: CellStyle =
    if context.hasFocus then style.focus.map(_.content).getOrElse(style.content)
    else style.content

object Select:
  /** @param label
    *   Converts each item to a display string. Defaults to [[Any.toString]].
    */
  def apply[A](
      size: Size,
      items: Seq[A],
      selected: Var[Int],
      style: TextStyle => TextStyle = identity,
      label: A => String = (a: A) => a.toString
  )(using ctx: Layout): Unit =
    ctx.addComponent { runtime =>
      val focusId = FocusId.next
      val context = new DefaultEvent(focusId, runtime) {}
      new Select(
        size,
        style(TextStyle.default),
        items,
        selected,
        label,
        context
      )
    }
