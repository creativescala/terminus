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

import terminus.ui.capability.Event
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
import terminus.ui.react.Reactive
import terminus.ui.style.BoxStyle
import terminus.ui.style.CellStyle
import terminus.ui.style.TextStyle
import terminus.ui.text

/** A component that displays a single line of text. The text is never wrapped,
  * so if it overflows the available width is will be truncated. This is
  * suitable for building buttons and other basic controls.
  */
final class Line(
    val size: Size,
    style: TextStyle,
    value: Reactive[text.Line],
    context: DefaultEvent
) extends Component:

  def react(using React): Unit =
    value.get
    ()

  def measure(constraint: Constraint): Dimensions =
    val line = value.peek
    val insets = activeBoxStyle.insets
    // The constraint we were given is for the whole box; shrink it to the space
    // available to the text content.
    val inner = insets.deflate(constraint)

    val targetWidth =
      size.width match
        case Measurement.Fixed(cells) => cells - insets.horizontal
        case Measurement.WrapContent  => line.width
        case _                        =>
          // Percentage, Weight and the intrinsics are resolved by the parent
          // into the incoming constraint, so here we simply fill the width on
          // offer. When that width is unbounded, fall back to the natural width.
          inner.maxWidth match
            case Infinity   => line.width
            case bound: Int => bound

    val contentWidth =
      val lowerBounded = targetWidth.max(inner.minWidth)
      inner.maxWidth match
        case Infinity   => lowerBounded
        case bound: Int => lowerBounded.min(bound)

    val contentHeight =
      size.height match
        case Measurement.Fixed(cells) => cells - insets.vertical
        case _                        => 1

    // Grow the content back to the full box, then guarantee the result still
    // satisfies the original (outer) constraint.
    constraint.constrain(
      insets.inflate(Dimensions(contentWidth, contentHeight))
    )

  def minIntrinsicWidth(height: Int | Infinity): Int =
    value.peek.length + activeBoxStyle.insets.horizontal

  def maxIntrinsicWidth(height: Int | Infinity): Int =
    value.peek.length + activeBoxStyle.insets.horizontal

  def minIntrinsicHeight(width: Int | Infinity): Int =
    1 + activeBoxStyle.insets.vertical

  /** Height beyond which the component would not grow, given a width. */
  def maxIntrinsicHeight(width: Int | Infinity): Int =
    1 + activeBoxStyle.insets.vertical

  def render(dimensions: Dimensions, buf: Buffer): Unit =
    val ab = activeBoxStyle
    val ac = activeContentStyle

    // The component draws from its own origin; the incoming buffer is already a
    // view clipped to this component's slot.
    val bounds = Rect(0, 0, dimensions.width, dimensions.height)
    Box.render(bounds, ab, buf)
    val inner = Box.innerRect(bounds, ab)

    val textBuf = buf.view(inner)
    textBuf.putLine(0, 0, value.peek, ac)

  private def activeBoxStyle: BoxStyle =
    if context.hasFocus then style.focus.map(_.box).getOrElse(style.box)
    else style.box

  private def activeContentStyle: CellStyle =
    if context.hasFocus then style.focus.map(_.content).getOrElse(style.content)
    else style.content
object Line:
  def apply(size: Size, style: TextStyle => TextStyle = identity)(
      body: Event ?=> Reactive[text.Line]
  )(using ctx: Layout): Unit =
    ctx.addComponent { runtime =>
      val focusId = FocusId.next
      val context = new DefaultEvent(focusId, runtime) {}
      val value = body(using context)

      new Line(size, style(TextStyle.default), value, context)
    }
