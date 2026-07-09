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

package terminus.ui.style

import munit.FunSuite
import terminus.ui.capability.Availability
import terminus.ui.capability.ComponentState
import terminus.ui.capability.Focus

class TextStyleSuite extends FunSuite:

  val unfocused = ComponentState(Focus.Unfocused, Availability.Enabled)
  val focused = ComponentState(Focus.Focused, Availability.Enabled)
  val disabled = ComponentState(Focus.Unfocused, Availability.Disabled)
  val focusedAndDisabled = ComponentState(Focus.Focused, Availability.Disabled)

  // ---------------------------------------------------------------------------
  // Base properties
  // ---------------------------------------------------------------------------

  test("the default style resolves to the default props in every state") {
    val style = TextStyle.default
    assertEquals(style(unfocused), TextProps.default)
    assertEquals(style(focused), TextProps.default)
    assertEquals(style(disabled), TextProps.default)
  }

  test("withBox(BoxStyle) replaces the base box style") {
    val noBorder = BoxStyle.default.withoutBorder
    val style = TextStyle.default.withBox(noBorder)
    assertEquals(style(unfocused).box, noBorder)
  }

  test("withBox(f) updates the current base box style") {
    val style = TextStyle.default.withBox(_.withPadding(2))
    assertEquals(style(unfocused).box.padding, 2)
  }

  test("withContent(CellStyle) replaces the base content style") {
    val bold = CellStyle.default.withBold
    val style = TextStyle.default.withContent(bold)
    assertEquals(style(unfocused).content, bold)
  }

  test("withContent(f) updates the current base content style") {
    val style = TextStyle.default.withContent(_.withItalic)
    assertEquals(style(unfocused).content.italic, true)
  }

  test("combinators do not change earlier style values") {
    val base = TextStyle.default
    val step1 = base.withBox(_.withoutBorder)
    val step2 = step1.withContent(_.withBold)

    assert(base(unfocused).box.border.isDefined)
    assertEquals(base(unfocused).content.bold, false)
    assertEquals(step1(unfocused).content.bold, false)
    assertEquals(step2(unfocused).box.border, None)
    assertEquals(step2(unfocused).content.bold, true)
  }

  // ---------------------------------------------------------------------------
  // State rules
  // ---------------------------------------------------------------------------

  test("a focused rule applies only in the focused state") {
    val style = TextStyle.default.focused(_.withContent(_.withBold))
    assertEquals(style(focused).content.bold, true)
    assertEquals(style(unfocused).content.bold, false)
  }

  test("a disabled rule applies only in the disabled state") {
    val style = TextStyle.default.disabled(_.withContent(_.withItalic))
    assertEquals(style(disabled).content.italic, true)
    assertEquals(style(unfocused).content.italic, false)
    assertEquals(style(focused).content.italic, false)
  }

  test("a state rule patches the base style rather than replacing it") {
    // The base customization (padding) must survive into the focused state
    // even though the focused rule doesn't mention it.
    val style = TextStyle.default
      .withBox(_.withPadding(2))
      .focused(_.withContent(_.withBold))
    assertEquals(style(focused).box.padding, 2)
    assertEquals(style(focused).content.bold, true)
  }

  test("rules for independent states combine when both match") {
    val style = TextStyle.default
      .focused(_.withContent(_.withBold))
      .disabled(_.withContent(_.withItalic))
    val props = style(focusedAndDisabled)
    assertEquals(props.content.bold, true)
    assertEquals(props.content.italic, true)
  }

  test("on conflict the later-declared rule wins") {
    val style = TextStyle.default
      .focused(_.withBox(_.withPadding(1)))
      .disabled(_.withBox(_.withPadding(3)))
    assertEquals(style(focusedAndDisabled).box.padding, 3)
    assertEquals(style(focused).box.padding, 1)
    assertEquals(style(disabled).box.padding, 3)
  }

  test("when adds a rule with an arbitrary predicate") {
    val style = TextStyle.default.when(_.focus == Focus.Unfocused)(
      _.withContent(_.withInvert)
    )
    assertEquals(style(unfocused).content.invert, true)
    assertEquals(style(focused).content.invert, false)
  }

  test("withBase applies under all existing rules") {
    val style = TextStyle.default
      .focused(_.withContent(_.withBold))
      .withBase(_.withContent(_.withItalic))
    assertEquals(style(unfocused).content.italic, true)
    val props = style(focused)
    assertEquals(props.content.italic, true)
    assertEquals(props.content.bold, true)
  }
