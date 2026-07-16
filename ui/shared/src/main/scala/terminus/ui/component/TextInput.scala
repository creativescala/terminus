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
import terminus.KeyCode
import terminus.ui.capability.Focus
import terminus.ui.capability.Layout
import terminus.ui.capability.Observe
import terminus.ui.event.DefaultEvent
import terminus.ui.event.FocusId
import terminus.ui.layout.Box
import terminus.ui.layout.Buffer
import terminus.ui.layout.Cell
import terminus.ui.layout.Component
import terminus.ui.layout.Constraint
import terminus.ui.layout.Dimensions
import terminus.ui.layout.Infinity
import terminus.ui.layout.Measurement
import terminus.ui.layout.Rect
import terminus.ui.layout.Size
import terminus.ui.react.WritableSignal
import terminus.ui.style.TextInputProps
import terminus.ui.style.TextInputStyle
import terminus.ui.text
import terminus.ui.text.Line

/** A single-line text input component.
  *
  * The caller owns `value` and can read it to react to what the user has typed:
  *
  * {{{
  * val name = signal(Line(""))
  * TextInput(Size.fixed(30, 1), name)
  * }}}
  *
  * The caller may also change `value` from outside the component; if the new
  * text is shorter than the old cursor position, the cursor is clamped to the
  * end of the new text (and is otherwise left where it was).
  *
  * Key bindings: printable characters insert at the cursor, Backspace / Delete
  * delete around the cursor, Left / Right / Home / End move the cursor. Text
  * longer than the visible area scrolls horizontally to keep the cursor in
  * view.
  */
final class TextInput(
    val size: Size,
    style: TextInputStyle = TextInputStyle.default,
    value: WritableSignal[Line],
    context: DefaultEvent
) extends Component:
  private val cursor = WritableSignal(0)

  /** The effective cursor position. The caller owns `value` and may have shrunk
    * it since the cursor last moved, so the stored position is clamped at every
    * read rather than written back — nothing observable ever sees an
    * out-of-range cursor.
    */
  private def cursorPos: Int =
    cursor.peek.min(value.peek.length)

  context.onKey(Key.left) {
    cursor.set((cursorPos - 1).max(0))
  }

  context.onKey(Key.right) {
    cursor.set((cursorPos + 1).min(value.peek.length))
  }

  context.onKey(Key.home) {
    cursor.set(0)
  }

  context.onKey(Key.`end`) {
    cursor.set(value.peek.length)
  }

  context.onKey(Key.backspace) {
    val pos = cursorPos
    if pos > 0 then
      value.update(line => line.delete(pos - 1))
      cursor.set(pos - 1)
  }
  context.onKey(Key.delete) {
    value.update(line => line.delete(cursorPos))
  }
  context.onAnyKey { key =>
    key.code match
      case KeyCode.Character(c) if !c.isControl =>
        val pos = cursorPos
        value.update(line => line.insert(pos, c))
        cursor.set(pos + 1)
      case _ => ()
  }

  def measure(constraint: Constraint)(using Observe): Dimensions =
    val line = value.get
    val insets = activeProps.box.insets
    // The constraint we were given is for the whole box; shrink it to the space
    // available to the text content.
    val inner = insets.deflate(constraint)

    val naturalWidth = line.width
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
    maxIntrinsicWidth(height)

  def maxIntrinsicWidth(height: Int | Infinity)(using Observe): Int =
    // By definition this component is a single line
    value.get.width + activeProps.box.insets.horizontal

  def minIntrinsicHeight(width: Int | Infinity)(using Observe): Int =
    maxIntrinsicHeight(width)

  def maxIntrinsicHeight(width: Int | Infinity)(using Observe): Int =
    activeProps.box.insets.vertical + 1

  def render(dimensions: Dimensions, buf: Buffer)(using Observe): Unit =
    val ab = activeProps.box
    val ac = activeProps.content

    // The component draws from its own origin; the incoming buffer is already a
    // view clipped to this component's slot.
    val bounds = Rect(0, 0, dimensions.width, dimensions.height)
    Box.render(bounds, ab, buf)
    val inner = Box.innerRect(bounds, ab)
    if inner.width <= 0 then return

    val line = value.get
    val pos = cursor.get.min(line.length)

    // Scroll to keep the cursor in view: cursor sits at the right edge when
    // the text overflows, and at its natural position otherwise.
    val scroll = (pos - inner.width + 1).max(0)
    val visible =
      line.substring(scroll, (scroll + inner.width).min(line.length))
    val cursorCol = pos - scroll // always in [0, inner.width)

    buf.putLine(inner.x, inner.y, visible, ac)

    // When the control is active draw the cursor by inverting the cell at the cursor column.
    context.focus.peek match
      case Focus.Unfocused => ()
      case Focus.Focused   =>
        val cursorChar =
          if cursorCol < visible.length then visible(cursorCol) else ' '
        buf.put(
          inner.x + cursorCol,
          inner.y,
          Cell(cursorChar.toInt, activeProps.cursor)
        )

  private def activeProps(using Observe): TextInputProps =
    style(context.state)

object TextInput:
  def apply(
      size: Size,
      style: TextInputStyle => TextInputStyle = identity,
      value: WritableSignal[Line]
  )(using ctx: Layout): Unit =
    ctx.addComponent { runtime =>
      val focusId = FocusId.next
      val context = new DefaultEvent(focusId, runtime) {}
      new TextInput(size, style(TextInputStyle.default), value, context)
    }
