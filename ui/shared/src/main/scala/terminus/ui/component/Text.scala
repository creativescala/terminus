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
import terminus.ui.layout.Constraint
import terminus.ui.layout.Dimensions
import terminus.ui.layout.Infinity
import terminus.ui.layout.Measurement
import terminus.ui.layout.Rect
import terminus.ui.layout.Size
import terminus.ui.capability.Layout
import terminus.ui.style.BoxStyle
import terminus.ui.style.CellStyle
import terminus.ui.style.TextStyle
import terminus.ui.layout.Box
import terminus.ui.react.Reactive
import terminus.ui.event.DefaultEvent
import terminus.ui.layout.DefaultLayout
import terminus.ui.capability.Event
import terminus.ui.capability.Layout
import terminus.ui.capability.React
import terminus.ui.text
import terminus.ui.text.Line

final class Text(
    val size: Size,
    style: TextStyle,
    text: Reactive[text.Text],
    context: DefaultEvent & DefaultLayout
) extends Component:
  def react(): Unit =
    text.get(React.empty)
    ()

  def measure(constraint: Constraint): Dimensions =
    val t = text.peek
    val insets = activeBox.insets
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
      text.peek.lines
        .flatMap(line => line.value.split(' '))
        .filter(_.nonEmpty)
        .map(word => Line(word).width)
        .maxOption
        .getOrElse(0)
    widestWord + activeBox.insets.horizontal

  def maxIntrinsicWidth(height: Int | Infinity): Int =
    // Text never grows wider than its widest unwrapped line, whatever the
    // available height.
    naturalContentWidth + activeBox.insets.horizontal

  def minIntrinsicHeight(width: Int | Infinity): Int =
    // Text height is fully determined by width, so the minimum and maximum
    // intrinsic heights coincide.
    maxIntrinsicHeight(width)

  def maxIntrinsicHeight(width: Int | Infinity): Int =
    val insets = activeBox.insets
    val lineCount =
      width match
        case Infinity      => text.peek.lines.length
        case w: Int        => text.peek.reflow((w - insets.horizontal).max(0)).length
    lineCount + insets.vertical

  /** The width of the widest logical (unwrapped) line of content, in cells. */
  private def naturalContentWidth: Int =
    text.peek.lines.map(_.width).maxOption.getOrElse(0)

  private def activeBox: BoxStyle =
    if context.hasFocus then style.focus.map(_.box).getOrElse(style.box)
    else style.box

  private def activeContent: CellStyle =
    if context.hasFocus then style.focus.map(_.content).getOrElse(style.content)
    else style.content
object Text:
  /** Create a Text component.
    *
    * When `height` is SizeToContent (the default) the height is computed from
    * the content: the number of `\n`-separated lines plus any border/padding
    * overhead. Pass an explicit positive `height` to fix the size regardless of
    * content.
    *
    * If `box.focused` is set, that style is used in place of `box` whenever the
    * component is inside a focused [[terminus.ui.FocusScope]].
    */
  def component(
      width: Int,
      height: Int | SizeToContent,
      style: TextStyle,
      text: LeafContent[String]
  )(using rc: RenderContext): Component =
    new Component:

      def size: Size =
        val ab = activeBox
        height match
          case SizeToContent =>
            val offset = (if ab.border.isDefined then 1 else 0) + ab.padding
            val lineCount = text.split('\n').length.max(1)
            Size.fixed(width, lineCount + 2 * offset)

          case height: Int => Size.fixed(width, height)

      def render(bounds: Rect, buf: Buffer): Unit =
        val ab = activeBox
        Box.render(bounds, ab, buf)
        val inner = Box.innerRect(bounds, ab)
        buf.putString(inner.x, inner.y, text, activeContent)

  def apply(
      width: Int,
      height: Int | SizeToContent = SizeToContent,
      style: TextStyle => TextStyle = identity
  )(
      text: LeafContent[String]
  )(using lc: Layout, rc: RenderContext): Unit =
    lc.add(component(width, height, style(TextStyle.default), text))
