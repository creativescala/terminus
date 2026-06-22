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

class TextSuite extends FunSuite:

  /** The String content of each logical line of `Text(s)`. */
  def lines(s: String): Seq[String] = Text(s).lines.map(_.value)

  /** The String content of each line of `Text(s)` reflowed to `width`. */
  def reflow(s: String, width: Int): Seq[String] =
    Text(s).reflow(width).map(_.value)

  // ---------------------------------------------------------------------------
  // Parsing into logical lines
  // ---------------------------------------------------------------------------

  test("a single line parses to one logical line") {
    assertEquals(lines("hello world"), Seq("hello world"))
  }

  test("newlines split into separate logical lines") {
    assertEquals(lines("one\ntwo\nthree"), Seq("one", "two", "three"))
  }

  test("blank lines from consecutive newlines are preserved") {
    assertEquals(lines("para one\n\npara two"), Seq("para one", "", "para two"))
  }

  test("a trailing newline produces a trailing empty line") {
    assertEquals(lines("text\n"), Seq("text", ""))
  }

  test("a leading newline produces a leading empty line") {
    assertEquals(lines("\ntext"), Seq("", "text"))
  }

  test("the empty string is a single empty line") {
    assertEquals(lines(""), Seq(""))
  }

  // ---------------------------------------------------------------------------
  // Line-ending normalization
  // ---------------------------------------------------------------------------

  test("CRLF is treated as a single break") {
    assertEquals(lines("one\r\ntwo"), Seq("one", "two"))
  }

  test("a lone CR is treated as a break") {
    assertEquals(lines("one\rtwo"), Seq("one", "two"))
  }

  test("mixed CRLF and LF normalize consistently") {
    assertEquals(lines("a\r\nb\nc"), Seq("a", "b", "c"))
  }

  // ---------------------------------------------------------------------------
  // Per-line sanitization
  // ---------------------------------------------------------------------------

  test("each logical line is sanitized") {
    assertEquals(lines("a\u001b[31mb\nc\td"), Seq("ab", "c       d"))
  }

  // ---------------------------------------------------------------------------
  // Reflow
  // ---------------------------------------------------------------------------

  test("reflow wraps each paragraph to the given width") {
    assertEquals(
      reflow("the quick brown fox", 10),
      Seq("the quick", "brown fox")
    )
  }

  test("reflow preserves the blank line between paragraphs") {
    assertEquals(
      reflow("aaa bbb ccc\n\nddd", 7),
      Seq("aaa bbb", "ccc", "", "ddd")
    )
  }

  test("reflow concatenates the wrapped lines of every logical line") {
    assertEquals(
      reflow("one two\nthree four", 4),
      Seq("one", "two", "thre", "e", "four")
    )
  }

end TextSuite
