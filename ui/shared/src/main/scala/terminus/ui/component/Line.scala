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
import terminus.ui.capability.Observe
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
import terminus.ui.react.DefaultReact
import terminus.ui.react.Signal
import terminus.ui.style.LineProps
import terminus.ui.style.LineStyle
import terminus.ui.text

/** A component that displays a single line of text. The text is never wrapped,
  * so if it overflows the available width it will be truncated.
  */
final class Line(
    val size: Size,
    style: LineStyle,
    value: Signal[text.Line],
    context: DefaultEvent
) extends Component:

  def measure(constraint: Constraint)(using Observe): Dimensions =
    val line = value.get
    val insets = activeProps.box.insets
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

  def minIntrinsicWidth(height: Int | Infinity)(using Observe): Int =
    value.get.length + activeProps.box.insets.horizontal

  def maxIntrinsicWidth(height: Int | Infinity)(using Observe): Int =
    value.get.length + activeProps.box.insets.horizontal

  def minIntrinsicHeight(width: Int | Infinity)(using Observe): Int =
    1 + activeProps.box.insets.vertical

  /** Height beyond which the component would not grow, given a width. */
  def maxIntrinsicHeight(width: Int | Infinity)(using Observe): Int =
    1 + activeProps.box.insets.vertical

  def render(dimensions: Dimensions, buf: Buffer)(using Observe): Unit =
    val ab = activeProps.box
    val ac = activeProps.content

    // The component draws from its own origin; the incoming buffer is already a
    // view clipped to this component's slot.
    val bounds = Rect(0, 0, dimensions.width, dimensions.height)
    Box.render(bounds, ab, buf)
    val inner = Box.innerRect(bounds, ab)

    val textBuf = buf.view(inner)
    textBuf.putLine(0, 0, value.get, ac)

  private def activeProps(using Observe): LineProps =
    style(context.state)
object Line:
  def apply(size: Size, style: LineStyle => LineStyle = identity)(
      body: (Event & React) ?=> Signal[text.Line]
  )(using ctx: Layout): Unit =
    ctx.addComponent { runtime =>
      val focusId = FocusId.next
      val context =
        new DefaultEvent(focusId, runtime) with DefaultReact(runtime) {}
      val value = body(using context)

      new Line(size, style(LineStyle.default), value, context)
    }
