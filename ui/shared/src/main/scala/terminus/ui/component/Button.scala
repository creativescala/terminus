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
import terminus.ui.capability.Event
import terminus.ui.capability.Layout
import terminus.ui.capability.React
import terminus.ui.capability.Submit
import terminus.ui.event.DefaultEvent
import terminus.ui.event.DefaultSubmit
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
import terminus.ui.react.Constant
import terminus.ui.react.Reactive
import terminus.ui.style.ButtonProps
import terminus.ui.style.ButtonStyle
import terminus.ui.text.Line

/** A single-line button, activated with Enter or Space.
  *
  * The body registers a submit handler and, optionally, an availability
  * condition, then returns the button's label:
  *
  * {{{
  * Button(Size.fixed(12, 3)) { ctx ?=>
  *   ctx.onSubmit { save() }
  *   ctx.enabledWhen(formValid)
  *   Constant(Line("Save"))
  * }
  * }}}
  *
  * Registering the submit handler makes the button focusable. A disabled button
  * is skipped by focus traversal, does not activate, and renders with any
  * `disabled` style rules applied over its base style.
  */
final class Button(
    val size: Size,
    style: ButtonStyle,
    label: Reactive[Line],
    context: DefaultEvent
) extends Component:

  def react(using React): Unit =
    label.get
    context.availability.get
    context.focus.get
    ()

  def measure(constraint: Constraint): Dimensions =
    val insets = activeProps.box.insets
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
        case _                        => 1

    constraint.constrain(
      insets.inflate(Dimensions(contentWidth, contentHeight))
    )

  def minIntrinsicWidth(height: Int | Infinity): Int =
    naturalWidth + activeProps.box.insets.horizontal

  def maxIntrinsicWidth(height: Int | Infinity): Int =
    naturalWidth + activeProps.box.insets.horizontal

  def minIntrinsicHeight(width: Int | Infinity): Int =
    1 + activeProps.box.insets.vertical

  def maxIntrinsicHeight(width: Int | Infinity): Int =
    1 + activeProps.box.insets.vertical

  def render(dimensions: Dimensions, buf: Buffer): Unit =
    val ab = activeProps.box
    val ac = activeProps.content

    // The component draws from its own origin; the incoming buffer is already a
    // view clipped to this component's slot.
    val bounds = Rect(0, 0, dimensions.width, dimensions.height)
    Box.render(bounds, ab, buf)
    val inner = Box.innerRect(bounds, ab)
    if inner.width <= 0 || inner.height <= 0 then return

    val raw = label.peek.value
    val line = Line(raw.take(inner.width).padTo(inner.width, ' '))
    buf.putLine(inner.x, inner.y, line, ac)

  private def naturalWidth: Int = label.peek.width

  private def activeProps: ButtonProps =
    style(context.state)

object Button:
  def apply(size: Size, style: ButtonStyle => ButtonStyle = identity)(
      body: Event & Submit[Unit] ?=> Reactive[Line]
  )(using ctx: Layout): Unit =
    ctx.addComponent { runtime =>
      val focusId = FocusId.next
      val context = new DefaultEvent(focusId, runtime)
        with DefaultSubmit[Unit](
          focusId,
          runtime,
          Seq(Key.enter, Key.space),
          Constant(())
        ) {}
      val label = body(using context)

      new Button(size, style(ButtonStyle.default), label, context)
    }
