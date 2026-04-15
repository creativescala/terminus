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

package terminus.ui

import munit.FunSuite

class CharWidthSuite extends FunSuite:

  /** Convenience: get the width of the first code point in a string literal. */
  def width(s: String): Int = CharWidth.of(s.codePointAt(0))

  // ---------------------------------------------------------------------------
  // Width 1 — narrow characters
  // ---------------------------------------------------------------------------

  test("ASCII letters are width 1") {
    assertEquals(width("A"), 1)
    assertEquals(width("z"), 1)
  }

  test("ASCII digits are width 1") {
    assertEquals(width("0"), 1)
    assertEquals(width("9"), 1)
  }

  test("ASCII punctuation is width 1") {
    assertEquals(width("!"), 1)
    assertEquals(width("-"), 1)
  }

  test("Latin Extended letters are width 1") {
    assertEquals(width("é"), 1) // U+00E9
    assertEquals(width("ñ"), 1) // U+00F1
  }

  test("Greek letters are width 1") {
    assertEquals(width("α"), 1) // U+03B1
    assertEquals(width("Ω"), 1) // U+03A9
  }

  // ---------------------------------------------------------------------------
  // Width 0 — zero-width characters
  // ---------------------------------------------------------------------------

  test("combining grave accent is width 0") {
    assertEquals(width("\u0300"), 0) // COMBINING GRAVE ACCENT
  }

  test("combining acute accent is width 0") {
    assertEquals(width("\u0301"), 0) // COMBINING ACUTE ACCENT
  }

  test("zero width joiner is width 0") {
    assertEquals(width("\u200D"), 0) // ZWJ (used in emoji sequences)
  }

  test("variation selector 16 is width 0") {
    assertEquals(width("\uFE0F"), 0) // emoji presentation selector
  }

  test("zero width space is width 0") {
    assertEquals(width("\u200B"), 0)
  }

  // ---------------------------------------------------------------------------
  // Width 2 — CJK and East Asian Wide
  // ---------------------------------------------------------------------------

  test("CJK unified ideographs are width 2") {
    assertEquals(width("中"), 2) // U+4E2D
    assertEquals(width("日"), 2) // U+65E5
    assertEquals(width("한"), 2) // U+D55C Hangul
  }

  test("Hiragana and Katakana are width 2") {
    assertEquals(width("あ"), 2) // U+3042 Hiragana
    assertEquals(width("ア"), 2) // U+30A2 Katakana
  }

  test("fullwidth Latin letters are width 2") {
    assertEquals(width("Ａ"), 2) // U+FF21 FULLWIDTH LATIN CAPITAL A
  }

  // ---------------------------------------------------------------------------
  // Width 2 — Emoji (Miscellaneous Symbols and Dingbats)
  // ---------------------------------------------------------------------------

  test("sparkles ✨ (U+2728) is width 2") {
    assertEquals(width("✨"), 2)
  }

  test("cross mark ❌ (U+274C) is width 2") {
    assertEquals(width("❌"), 2)
  }

  test("check mark ✅ (U+2705) is width 2") {
    assertEquals(width("✅"), 2)
  }

  test("raised fist ✊ (U+270A) is width 2") {
    assertEquals(width("✊"), 2)
  }

  test("sun ☀ (U+2600) is width 2") {
    assertEquals(width("☀"), 2)
  }

  test("warning sign ⚠ (U+26A0) is width 2") {
    assertEquals(width("⚠"), 2)
  }

  // ---------------------------------------------------------------------------
  // Width 2 — Emoji above U+1F000 (Supplementary Multilingual Plane)
  // ---------------------------------------------------------------------------

  test("grinning face 😀 (U+1F600) is width 2") {
    assertEquals(width("😀"), 2)
  }

  test("flexed biceps 💪 (U+1F4AA) is width 2") {
    assertEquals(width("💪"), 2)
  }

  test("red circle 🔴 (U+1F534) is width 2") {
    assertEquals(width("🔴"), 2)
  }

  test("green circle 🟢 (U+1F7E2) is width 2") {
    assertEquals(width("🟢"), 2)
  }

  test("blue circle 🔵 (U+1F535) is width 2") {
    assertEquals(width("🔵"), 2)
  }

  test("counterclockwise arrows 🔄 (U+1F504) is width 2") {
    assertEquals(width("🔄"), 2)
  }
