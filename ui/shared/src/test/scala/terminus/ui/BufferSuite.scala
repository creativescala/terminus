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
import terminus.StringBuilderTerminal
import terminus.effect.AnsiCodes
import terminus.ui.style.CellStyle

class BufferSuite extends FunSuite:

  // Shorthand escape-code strings used in assertions
  val reset: String = AnsiCodes.sgr("0")
  val boldOn: String = AnsiCodes.format.bold.on

  /** Run f with a StringBuilderTerminal and return the captured output. */
  def capture(f: Terminal ?=> Unit): String =
    val t = StringBuilderTerminal()
    f(using t)
    t.result()

  /** Move cursor to 1-based (col, row). */
  def moveTo(col: Int, row: Int): String = AnsiCodes.cursor.to(col, row)

  // ---------------------------------------------------------------------------
  // renderDiff — no changes
  // ---------------------------------------------------------------------------

  test("renderDiff emits only resets when nothing has changed") {
    val prev = Buffer(3, 1)
    val curr = Buffer(3, 1)
    prev.put(0, 0, Cell('A'.toInt, CellStyle.default))
    curr.put(0, 0, Cell('A'.toInt, CellStyle.default))

    val out = capture(curr.renderDiff(prev))
    assertEquals(out, reset + reset)
  }

  // ---------------------------------------------------------------------------
  // renderDiff — single cell changed
  // ---------------------------------------------------------------------------

  test("renderDiff moves to the changed cell and writes it") {
    val prev = Buffer(3, 1)
    val curr = Buffer(3, 1)
    prev.put(0, 0, Cell('A'.toInt, CellStyle.default))
    curr.put(0, 0, Cell('B'.toInt, CellStyle.default))

    val out = capture(curr.renderDiff(prev))
    assertEquals(out, reset + moveTo(1, 1) + "B" + reset)
  }

  test("renderDiff writes a changed cell not at the origin") {
    val prev = Buffer(3, 1)
    val curr = Buffer(3, 1)
    prev.put(2, 0, Cell('A'.toInt, CellStyle.default))
    curr.put(2, 0, Cell('Z'.toInt, CellStyle.default))

    val out = capture(curr.renderDiff(prev))
    assertEquals(out, reset + moveTo(3, 1) + "Z" + reset)
  }

  // ---------------------------------------------------------------------------
  // renderDiff — cursor optimisation: adjacent changed cells
  // ---------------------------------------------------------------------------

  test("renderDiff suppresses cursor move for adjacent changed cells") {
    val prev = Buffer(3, 1)
    val curr = Buffer(3, 1)
    // Both cells at x=0 and x=1 change
    prev.put(0, 0, Cell('A'.toInt, CellStyle.default))
    prev.put(1, 0, Cell('B'.toInt, CellStyle.default))
    curr.put(0, 0, Cell('C'.toInt, CellStyle.default))
    curr.put(1, 0, Cell('D'.toInt, CellStyle.default))

    val out = capture(curr.renderDiff(prev))
    // Only one cursor.to at the start; 'D' follows 'C' without a move
    assertEquals(out, reset + moveTo(1, 1) + "C" + "D" + reset)
  }

  test("renderDiff emits cursor move for non-adjacent changed cells") {
    val prev = Buffer(4, 1)
    val curr = Buffer(4, 1)
    // Cells at x=0 and x=3 change; x=1 and x=2 are unchanged
    prev.put(0, 0, Cell('A'.toInt, CellStyle.default))
    prev.put(3, 0, Cell('B'.toInt, CellStyle.default))
    curr.put(0, 0, Cell('X'.toInt, CellStyle.default))
    curr.put(3, 0, Cell('Y'.toInt, CellStyle.default))

    val out = capture(curr.renderDiff(prev))
    assertEquals(out, reset + moveTo(1, 1) + "X" + moveTo(4, 1) + "Y" + reset)
  }

  // ---------------------------------------------------------------------------
  // renderDiff — changes on different rows
  // ---------------------------------------------------------------------------

  test("renderDiff handles changes on different rows") {
    val prev = Buffer(2, 2)
    val curr = Buffer(2, 2)
    prev.put(0, 0, Cell('A'.toInt, CellStyle.default))
    prev.put(0, 1, Cell('B'.toInt, CellStyle.default))
    curr.put(0, 0, Cell('X'.toInt, CellStyle.default))
    curr.put(0, 1, Cell('Y'.toInt, CellStyle.default))

    val out = capture(curr.renderDiff(prev))
    assertEquals(
      out,
      reset + moveTo(1, 1) + "X" + moveTo(1, 2) + "Y" + reset
    )
  }

  // ---------------------------------------------------------------------------
  // renderDiff — style changes
  // ---------------------------------------------------------------------------

  test("renderDiff emits SGR codes when style changes") {
    val prev = Buffer(2, 1)
    val curr = Buffer(2, 1)
    prev.put(0, 0, Cell('A'.toInt, CellStyle.default))
    curr.put(0, 0, Cell('A'.toInt, CellStyle(bold = true)))

    val out = capture(curr.renderDiff(prev))
    assertEquals(out, reset + moveTo(1, 1) + boldOn + "A" + reset)
  }

  test("renderDiff does not emit SGR codes when style is unchanged") {
    val prev = Buffer(2, 1)
    val curr = Buffer(2, 1)
    prev.put(0, 0, Cell('A'.toInt, CellStyle(bold = true)))
    curr.put(0, 0, Cell('B'.toInt, CellStyle(bold = true)))

    val out = capture(curr.renderDiff(prev))
    // bold was on from the reset start; no bold code needed before 'B'
    assertEquals(out, reset + moveTo(1, 1) + boldOn + "B" + reset)
  }

  // ---------------------------------------------------------------------------
  // renderDiff — wide characters
  // ---------------------------------------------------------------------------

  test("renderDiff skips continuation cells") {
    val prev = Buffer(4, 1)
    val curr = Buffer(4, 1)
    // Wide char 😀 at x=0 (continuation at x=1) unchanged; narrow char at x=2 changes
    val smile = "😀".codePointAt(0)
    prev.put(0, 0, Cell(smile, CellStyle.default))
    prev.put(1, 0, Cell.continuation)
    prev.put(2, 0, Cell('A'.toInt, CellStyle.default))
    curr.put(0, 0, Cell(smile, CellStyle.default))
    curr.put(1, 0, Cell.continuation)
    curr.put(2, 0, Cell('B'.toInt, CellStyle.default))

    val out = capture(curr.renderDiff(prev))
    assertEquals(out, reset + moveTo(3, 1) + "B" + reset)
  }

  test("renderDiff writes a changed wide character") {
    val prev = Buffer(4, 1)
    val curr = Buffer(4, 1)
    val smile = "😀".codePointAt(0)
    val wave = "👋".codePointAt(0)
    prev.put(0, 0, Cell(smile, CellStyle.default))
    prev.put(1, 0, Cell.continuation)
    curr.put(0, 0, Cell(wave, CellStyle.default))
    curr.put(1, 0, Cell.continuation)

    val out = capture(curr.renderDiff(prev))
    assertEquals(out, reset + moveTo(1, 1) + "👋" + reset)
  }

  // ---------------------------------------------------------------------------
  // renderDiff — dimension mismatch
  // ---------------------------------------------------------------------------

  test("renderDiff throws when buffer dimensions differ") {
    val prev = Buffer(3, 1)
    val curr = Buffer(4, 1)
    intercept[IllegalArgumentException] {
      capture(curr.renderDiff(prev))
    }
  }

  // ---------------------------------------------------------------------------
  // putString — newline handling
  // ---------------------------------------------------------------------------

  /** Render the whole buffer and return the captured output. */
  def renderFull(buf: Buffer): String =
    capture(buf.render)

  test("putString writes a simple string on one row") {
    val buf = Buffer(3, 1)
    buf.putString(0, 0, "ABC", CellStyle.default)
    val out = renderFull(buf)
    assertEquals(out, reset + moveTo(1, 1) + "A" + "B" + "C" + reset)
  }

  test("putString newline advances to next row and resets column") {
    val buf = Buffer(3, 2)
    buf.putString(0, 0, "AB\nCD", CellStyle.default)
    // Row 1: "AB", Row 2: "CD"
    val out = renderFull(buf)
    assertEquals(
      out,
      reset + moveTo(1, 1) + "A" + "B" + " " + moveTo(
        1,
        2
      ) + "C" + "D" + " " + reset
    )
  }

  test("putString respects starting x when resetting after newline") {
    val buf = Buffer(4, 2)
    buf.putString(1, 0, "AB\nCD", CellStyle.default)
    // Starts at col 1; after newline resets to col 1 on row 2
    val out = renderFull(buf)
    assertEquals(
      out,
      reset + moveTo(1, 1) + " " + "A" + "B" + " " + moveTo(
        1,
        2
      ) + " " + "C" + "D" + " " + reset
    )
  }
