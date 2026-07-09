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
import terminus.ui.style.TextProps
import terminus.ui.style.TextStyle
import terminus.ui.text
import terminus.ui.text.Line

final class Text(
    val size: Size,
    style: TextStyle,
    value: Reactive[text.Text],
    context: DefaultEvent
) extends Component:

  def react(using React): Unit =
    value.get
    context.focus.get
    context.availability.get
    ()

  def measure(constraint: Constraint): Dimensions =
    val t = value.peek
    val insets = activeProps.box.insets
    // The constraint we were given is for the whole box; shrink it to the space
    // available to the text content.
    val inner = insets.deflate(constraint)

    // Resolve content width first: text reflows to a width, and the resulting
    // line count then determines the height (height-for-width).
    val naturalWidth = naturalContentWidth
    val targetWidth =
      size.width match
        case Measurement.Fixed(cells) => cells - insets.horizontal
        case Measurement.WrapContent  => naturalWidth
        case _                        =>
          // Percentage, Weight and the intrinsics are resolved by the parent
          // into the incoming constraint, so here we simply fill the width on
          // offer. When that width is unbounded, fall back to the natural width.
          inner.maxWidth match
            case Infinity   => naturalWidth
            case bound: Int => bound
    val contentWidth =
      val lowerBounded = targetWidth.max(inner.minWidth)
      inner.maxWidth match
        case Infinity   => lowerBounded
        case bound: Int => lowerBounded.min(bound)

    val lines = t.reflow(contentWidth)
    val contentHeight =
      size.height match
        case Measurement.Fixed(cells) => cells - insets.vertical
        case _                        => lines.length

    // Grow the content back to the full box, then guarantee the result still
    // satisfies the original (outer) constraint.
    constraint.constrain(
      insets.inflate(Dimensions(contentWidth, contentHeight))
    )

  def minIntrinsicWidth(height: Int | Infinity): Int =
    // The narrowest the text can be without breaking a word across lines is the
    // width of its widest single word. Height does not affect text width.
    val widestWord =
      value.peek.lines
        .flatMap(line => line.value.split(' '))
        .filter(_.nonEmpty)
        .map(word => Line(word).width)
        .maxOption
        .getOrElse(0)
    widestWord + activeProps.box.insets.horizontal

  def maxIntrinsicWidth(height: Int | Infinity): Int =
    // Text never grows wider than its widest unwrapped line, whatever the
    // available height.
    naturalContentWidth + activeProps.box.insets.horizontal

  def minIntrinsicHeight(width: Int | Infinity): Int =
    // Text height is fully determined by width, so the minimum and maximum
    // intrinsic heights coincide.
    maxIntrinsicHeight(width)

  def maxIntrinsicHeight(width: Int | Infinity): Int =
    val insets = activeProps.box.insets
    val lineCount =
      width match
        case Infinity => value.peek.lines.length
        case w: Int   =>
          value.peek.reflow((w - insets.horizontal).max(0)).length
    lineCount + insets.vertical

  def render(dimensions: Dimensions, buf: Buffer): Unit =
    val ab = activeProps.box
    val ac = activeProps.content

    // The component draws from its own origin; the incoming buffer is already a
    // view clipped to this component's slot.
    val bounds = Rect(0, 0, dimensions.width, dimensions.height)
    Box.render(bounds, ab, buf)
    val inner = Box.innerRect(bounds, ab)

    val textBuf = buf.view(inner)
    var row = 0
    value.peek.reflow(inner.width).foreach { line =>
      textBuf.putLine(0, row, line, ac)
      row = row + 1
    }

  /** The width of the widest logical (unwrapped) line of content, in cells. */
  private def naturalContentWidth: Int =
    value.peek.lines.map(_.width).maxOption.getOrElse(0)

  private def activeProps: TextProps =
    style(context.state)
object Text:
  def apply(size: Size, style: TextStyle => TextStyle = identity)(
      body: Event ?=> Reactive[text.Text]
  )(using ctx: Layout): Unit =
    ctx.addComponent { runtime =>
      val focusId = FocusId.next
      val context = new DefaultEvent(focusId, runtime) {}
      val value = body(using context)

      new Text(size, style(TextStyle.default), value, context)
    }
