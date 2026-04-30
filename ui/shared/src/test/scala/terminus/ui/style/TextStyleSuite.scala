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

class TextStyleSuite extends FunSuite:

  // ---------------------------------------------------------------------------
  // withBox — copy-on-write
  // ---------------------------------------------------------------------------

  test("withBox returns a new instance, not the original") {
    val original = TextStyle.default
    val updated = original.withBox(_.withoutBorder)
    assertNotEquals(updated, original)
  }

  test("withBox does not mutate the original") {
    val original = TextStyle.default
    val before = original.box
    original.withBox(_.withoutBorder)
    assertEquals(original.box, before)
    assertEquals(original.box.border, Some(Border.single))
  }

  test("withBox(BoxStyle) replaces the box style") {
    val noBorder = BoxStyle.default.withoutBorder
    val updated = TextStyle.default.withBox(noBorder)
    assertEquals(updated.box, noBorder)
  }

  test("withBox(f) applies the function to the current box style") {
    val updated = TextStyle.default.withBox(_.withPadding(2))
    assertEquals(updated.box.padding, 2)
  }

  // ---------------------------------------------------------------------------
  // withContent — copy-on-write
  // ---------------------------------------------------------------------------

  test("withContent returns a new instance, not the original") {
    val original = TextStyle.default
    val updated = original.withContent(_.withBold)
    assertNotEquals(updated, original)
  }

  test("withContent does not mutate the original") {
    val original = TextStyle.default
    val before = original.content
    original.withContent(_.withBold)
    assertEquals(original.content, before)
  }

  test("withContent(CellStyle) replaces the content style") {
    val bold = CellStyle.default.withBold
    val updated = TextStyle.default.withContent(bold)
    assertEquals(updated.content, bold)
  }

  test("withContent(f) applies the function to the current content style") {
    val updated = TextStyle.default.withContent(_.withItalic)
    assertEquals(updated.content.italic, true)
  }

  // ---------------------------------------------------------------------------
  // withFocus / withoutFocus — copy-on-write
  // ---------------------------------------------------------------------------

  test("TextStyle has no focus style by default") {
    assertEquals(TextStyle.default.focus, None)
  }

  test("withFocus(f) returns a new instance") {
    val original = TextStyle.default
    val updated = original.withFocus(_.withBox(_.withoutBorder))
    assertNotEquals(updated, original)
  }

  test("withFocus(f) does not mutate the original") {
    val original = TextStyle.default
    val before = original.focus.map(_.box)
    original.withFocus(_.withBox(_.withoutBorder))
    assertEquals(original.focus.map(_.box), before)
  }

  test("withFocus(f) updates the focused box style") {
    val updated = TextStyle.default.withFocus(_.withBox(_.withoutBorder))
    assertEquals(updated.focus.map(_.box.border), Some(None))
  }

  test("withFocus(f) updates the focused content style") {
    val updated = TextStyle.default.withFocus(_.withContent(_.withBold))
    assertEquals(updated.focus.map(_.content.bold), Some(true))
  }

  test("withoutFocus removes the focus style") {
    val updated = TextStyle.default.withoutFocus
    assertEquals(updated.focus, None)
  }

  test("withFocus(f) creates focus from default even after withoutFocus") {
    val noFocus = TextStyle.default.withoutFocus
    val attempted = noFocus.withFocus(_.withContent(_.withBold))
    assertEquals(attempted.focus.map(_.content.bold), Some(true))
  }

  // ---------------------------------------------------------------------------
  // Chaining
  // ---------------------------------------------------------------------------

  test("method calls can be chained") {
    val style = TextStyle.default
      .withBox(_.withoutBorder)
      .withContent(_.withBold)
      .withFocus(_.withContent(_.withItalic))

    assertEquals(style.box.border, None)
    assertEquals(style.content.bold, true)
    assertEquals(style.focus.map(_.content.italic), Some(true))
  }

  test("chaining does not mutate intermediate results") {
    val base = TextStyle.default
    val step1 = base.withBox(_.withoutBorder)
    val step2 = step1.withContent(_.withBold)

    // base unaffected
    assert(base.box.border.isDefined)
    assertEquals(base.content.bold, false)
    // step1 unaffected by step2
    assertEquals(step2.box.border, None)
    assertEquals(step1.content.bold, false)
    // step2 has both changes
    assertEquals(step2.box.border, None)
    assertEquals(step2.content.bold, true)
  }
