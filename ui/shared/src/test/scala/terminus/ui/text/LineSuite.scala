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
    assertEquals(
      reflow("supercalifragilistic", 7),
      Seq("superca", "lifragi", "listic")
    )
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

  // ---------------------------------------------------------------------------
  // apply / length — character indexing (not display cells)
  // ---------------------------------------------------------------------------

  test("apply returns the character at an index") {
    assertEquals(Line("abc")(0), 'a')
    assertEquals(Line("abc")(2), 'c')
  }

  test("length counts characters, not display cells") {
    // "漢字" is 2 characters but 4 cells (see width tests above).
    assertEquals(Line("漢字").length, 2)
    assertEquals(Line("漢字").width, 4)
  }

  test("length of the empty line is zero") {
    assertEquals(Line("").length, 0)
  }

  // ---------------------------------------------------------------------------
  // substring
  // ---------------------------------------------------------------------------

  test("substring returns the requested character range") {
    assertEquals(Line("hello").substring(1, 4).value, "ell")
  }

  test("substring of an empty range is the empty line") {
    assertEquals(Line("hello").substring(2, 2).value, "")
  }

  test("substring covering the whole line returns the whole line") {
    assertEquals(Line("hello").substring(0, 5).value, "hello")
  }

  // ---------------------------------------------------------------------------
  // delete
  // ---------------------------------------------------------------------------

  test("delete removes the character at a position") {
    assertEquals(Line("abc").delete(1).value, "ac")
  }

  test("delete at the first position removes the first character") {
    assertEquals(Line("abc").delete(0).value, "bc")
  }

  test("delete at the last position removes the last character") {
    assertEquals(Line("abc").delete(2).value, "ab")
  }

  test("delete at the length (past the end) leaves the line unchanged") {
    assertEquals(Line("abc").delete(3).value, "abc")
  }

  test("delete at a negative position leaves the line unchanged") {
    assertEquals(Line("abc").delete(-1).value, "abc")
  }

  // ---------------------------------------------------------------------------
  // insert
  // ---------------------------------------------------------------------------

  test("insert places a character in the middle") {
    assertEquals(Line("ac").insert(1, 'b').value, "abc")
  }

  test("insert at the start prepends a character") {
    assertEquals(Line("bc").insert(0, 'a').value, "abc")
  }

  test("insert at the length appends a character") {
    // Appending at the end is the common text-input case: the cursor sits at
    // position == length, and inserting there must extend the line.
    assertEquals(Line("ab").insert(2, 'c').value, "abc")
  }

  test("insert into an empty line at position 0 yields a one-character line") {
    // The first keystroke in an empty TextInput: cursor at 0, length 0.
    assertEquals(Line("").insert(0, 'x').value, "x")
  }

  test("insert past the end (beyond the length) leaves the line unchanged") {
    assertEquals(Line("ab").insert(3, 'c').value, "ab")
  }

  test("insert at a negative position leaves the line unchanged") {
    assertEquals(Line("ab").insert(-1, 'c').value, "ab")
  }

end LineSuite
