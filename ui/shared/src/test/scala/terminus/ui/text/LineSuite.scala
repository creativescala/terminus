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

package terminus.ui.text

import munit.FunSuite

class LineSuite extends FunSuite:

  /** The sanitized String content of a Line built from `s`. */
  def clean(s: String): String = Line(s).value

  /** The String content of each line produced by reflowing `s` to `width`. */
  def reflow(s: String, width: Int): Seq[String] =
    Line(s).reflow(width).map(_.value)

  // ---------------------------------------------------------------------------
  // Sanitization — plain text
  // ---------------------------------------------------------------------------

  test("plain ASCII is unchanged") {
    assertEquals(clean("hello world"), "hello world")
  }

  test("non-ASCII printable text is preserved") {
    assertEquals(clean("café résumé α漢字"), "café résumé α漢字")
  }

  test("empty string sanitizes to empty") {
    assertEquals(clean(""), "")
  }

  // ---------------------------------------------------------------------------
  // Sanitization — whitespace
  // ---------------------------------------------------------------------------

  test("newline becomes a space") {
    assertEquals(clean("a\nb"), "a b")
  }

  test("carriage return becomes a space") {
    assertEquals(clean("a\rb"), "a b")
  }

  test("tab expands to the next 8-column tab stop") {
    // "ab" is 2 columns; the next stop is column 8, so 6 spaces.
    assertEquals(clean("ab\tc"), "ab      c")
  }

  test("tab at column 0 expands to a full tab width") {
    assertEquals(clean("\tx"), "        x")
  }

  test("tab stop is measured in display cells, after a wide character") {
    // "漢" occupies 2 columns, so the tab fills the remaining 6 to reach 8.
    assertEquals(clean("漢\tx"), "漢      x")
  }

  // ---------------------------------------------------------------------------
  // Sanitization — escape sequences
  // ---------------------------------------------------------------------------

  test("CSI colour sequences are stripped whole, not just the ESC byte") {
    assertEquals(clean("\u001b[31mred\u001b[0m"), "red")
  }

  test("CSI with intermediate/parameter bytes is fully consumed") {
    assertEquals(clean("a\u001b[1;31mb"), "ab")
  }

  test("OSC terminated by BEL is stripped") {
    assertEquals(clean("x\u001b]0;window title\u0007y"), "xy")
  }

  test("OSC terminated by ST (ESC backslash) is stripped") {
    assertEquals(clean("x\u001b]0;window title\u001b\\y"), "xy")
  }

  test("two-character escape is stripped") {
    assertEquals(clean("a\u001bMb"), "ab")
  }

  test("a lone trailing ESC is dropped") {
    assertEquals(clean("ab\u001b"), "ab")
  }

  // ---------------------------------------------------------------------------
  // Sanitization — control and bidirectional characters
  // ---------------------------------------------------------------------------

  test("C0 control codes are dropped") {
    assertEquals(clean("a\u0001b\u0008c"), "abc")
  }

  test("DEL is dropped") {
    assertEquals(clean("a\u007fb"), "ab")
  }

  test("C1 control codes are dropped") {
    assertEquals(clean("a\u0085b"), "ab")
  }

  test("bidi override characters are dropped") {
    assertEquals(clean("a\u202eb"), "ab") // RIGHT-TO-LEFT OVERRIDE
  }

  test("bidi isolate characters are dropped") {
    assertEquals(clean("a\u2066b"), "ab") // LEFT-TO-RIGHT ISOLATE
  }

  test("combining marks are preserved (not control characters)") {
    assertEquals(clean("á"), "á")
  }

  // ---------------------------------------------------------------------------
  // Width
  // ---------------------------------------------------------------------------

  test("width of ASCII is one cell per character") {
    assertEquals(Line("hello").width, 5)
  }

  test("width counts wide characters as two cells") {
    assertEquals(Line("漢字").width, 4)
  }

  test("width counts combining marks as zero cells") {
    assertEquals(Line("á").width, 1)
  }

  test("width handles astral (surrogate-pair) code points") {
    assertEquals(Line("😀").width, 2)
  }

  // ---------------------------------------------------------------------------
  // Reflow
  // ---------------------------------------------------------------------------

  test("reflow wraps greedily on word boundaries") {
    assertEquals(
      reflow("the quick brown fox jumps", 10),
      Seq("the quick", "brown fox", "jumps")
    )
  }

  test("reflow keeps a line that exactly fits the width") {
    assertEquals(reflow("ab cd", 5), Seq("ab cd"))
  }

  test("reflow breaks when the separator would overflow the width") {
    assertEquals(reflow("ab cd", 4), Seq("ab", "cd"))
  }

  test("reflow hard-breaks a word longer than the width") {
    assertEquals(reflow("supercalifragilistic", 7), Seq("superca", "lifragi", "listic"))
  }

  test("reflow lets a following word continue after a hard-broken word") {
    assertEquals(reflow("hi abcdefgh", 3), Seq("hi", "abc", "def", "gh"))
  }

  test("reflow collapses runs of whitespace at break points") {
    assertEquals(reflow("a    b", 10), Seq("a b"))
  }

  test("reflow measures width in cells for wide characters") {
    // "漢字" is 4 cells and exactly fills the width; "ab" moves to the next line.
    assertEquals(reflow("漢字 ab", 4), Seq("漢字", "ab"))
  }

  test("reflow never splits a wide character across a hard break") {
    // Three wide characters, 6 cells, broken to width 3 = one char per line.
    assertEquals(reflow("漢字漢", 3), Seq("漢", "字", "漢"))
  }

  test("reflow of an empty line yields a single empty line") {
    assertEquals(reflow("", 5), Seq(""))
  }

  test("reflow with non-positive width returns the line unchanged") {
    assertEquals(reflow("anything goes", 0), Seq("anything goes"))
  }

end LineSuite
