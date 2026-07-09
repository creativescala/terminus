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
import terminus.ui.capability.React
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
import terminus.ui.react.Var
import terminus.ui.style.TextInputProps
import terminus.ui.style.TextInputStyle
import terminus.ui.text
import terminus.ui.text.Line

/** A single-line text input component.
  *
  * The caller owns `value` and can read it to react to what the user has typed:
  *
  * {{{
  * val name = Var(Line(""))
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
    value: Var[Line],
    context: DefaultEvent
) extends Component:
  private val cursor = Var(0)

  context.onKey(Key.left) {
    cursor.update(c => (c - 1).max(0))
  }

  context.onKey(Key.right) {
    cursor.update(c => (c + 1).min(value.peek.length))
  }

  context.onKey(Key.home) {
    cursor.set(0)
  }

  context.onKey(Key.`end`) {
    cursor.set(value.peek.length)
  }

  context.onKey(Key.backspace) {
    val pos = cursor.peek
    if pos > 0 then
      value.update(line => line.delete(pos - 1))
      cursor.update(_ - 1)
  }
  context.onKey(Key.delete) {
    val pos = cursor.peek
    value.update(line => line.delete(pos))
  }
  context.onAnyKey { key =>
    key.code match
      case KeyCode.Character(c) if !c.isControl =>
        val pos = cursor.peek
        value.update(line => line.insert(pos, c))
        cursor.update(_ + 1)
      case _ => ()
  }
  def react(using React): Unit =
    val line = value.get
    cursor.get
    // The caller owns `value` and may have replaced it since the last frame,
    // leaving the cursor beyond the end of the new text. Clamp it here so
    // layout, render, and subsequent key handlers always see a valid position.
    cursor.update(_.min(line.length))
    context.focus.get
    context.availability.get
    ()

  def measure(constraint: Constraint): Dimensions =
    val line = value.peek
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

  def minIntrinsicWidth(height: Int | Infinity): Int =
    maxIntrinsicWidth(height)

  def maxIntrinsicWidth(height: Int | Infinity): Int =
    // By definition this component is a single line
    value.peek.width + activeProps.box.insets.horizontal

  def minIntrinsicHeight(width: Int | Infinity): Int =
    maxIntrinsicHeight(width)

  def maxIntrinsicHeight(width: Int | Infinity): Int =
    activeProps.box.insets.vertical + 1

  def render(dimensions: Dimensions, buf: Buffer): Unit =
    val ab = activeProps.box
    val ac = activeProps.content

    // The component draws from its own origin; the incoming buffer is already a
    // view clipped to this component's slot.
    val bounds = Rect(0, 0, dimensions.width, dimensions.height)
    Box.render(bounds, ab, buf)
    val inner = Box.innerRect(bounds, ab)
    if inner.width <= 0 then return

    val line = value.peek
    val pos = cursor.peek

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

  private def activeProps: TextInputProps =
    style(context.state)

object TextInput:
  def apply(
      size: Size,
      style: TextInputStyle => TextInputStyle = identity,
      value: Var[Line]
  )(using ctx: Layout): Unit =
    ctx.addComponent { runtime =>
      val focusId = FocusId.next
      val context = new DefaultEvent(focusId, runtime) {}
      new TextInput(size, style(TextInputStyle.default), value, context)
    }
