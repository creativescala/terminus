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

class LayoutSuite extends FunSuite:

  /** A minimal component that writes a single character at its origin. */
  def cell(char: Char, width: Int = 1, height: Int = 1): Component =
    new Component:
      val size: Size = Size(width, height)
      def render(bounds: Rect, buf: Buffer): Unit =
        buf.put(bounds.x, bounds.y, Cell(char.toInt, CellStyle.default))

  /** A component that fills its entire bounds with a single character. */
  def filledCell(char: Char, width: Int = 1, height: Int = 1): Component =
    new Component:
      val size: Size = Size(width, height)
      def render(bounds: Rect, buf: Buffer): Unit =
        val c = Cell(char.toInt, CellStyle.default)
        var y = bounds.y
        while y < bounds.y + height do
          var x = bounds.x
          while x < bounds.x + width do
            buf.put(x, y, c)
            x += 1
          y += 1

  /** Render a component into a fresh buffer and return the terminal output. */
  def renderToString(component: Component): String =
    val buf = Buffer(component.size.width, component.size.height)
    component.render(
      Rect(0, 0, component.size.width, component.size.height),
      buf
    )
    val t = StringBuilderTerminal()
    buf.render(using t)
    t.result()

  val reset: String = AnsiCodes.sgr("0")
  def moveTo(col: Int, row: Int): String = AnsiCodes.cursor.to(col, row)

  // ---------------------------------------------------------------------------
  // Row — size accumulation
  // ---------------------------------------------------------------------------

  test("Row size is zero with no children") {
    val row = new Row()
    assertEquals(row.size, Size(0, 0))
  }

  test("Row size sums widths and takes max height") {
    val row = new Row()
    row.add(cell('A', width = 3, height = 2))
    row.add(cell('B', width = 5, height = 1))
    // width = 3 + 5 = 8, height = max(2, 1) = 2
    assertEquals(row.size, Size(8, 2))
  }

  // ---------------------------------------------------------------------------
  // Row — layout
  // ---------------------------------------------------------------------------

  test("Row places a single child at the origin") {
    val row = new Row()
    row.add(cell('A'))
    val out = renderToString(row)
    assertEquals(out, reset + moveTo(1, 1) + "A" + reset)
  }

  test("Row places two children left to right") {
    val row = new Row()
    row.add(cell('A'))
    row.add(cell('B'))
    // Both in row 1; 'A' at col 1, 'B' at col 2 (adjacent — no extra moveTo)
    val out = renderToString(row)
    assertEquals(out, reset + moveTo(1, 1) + "A" + "B" + reset)
  }

  test("Row places three children left to right") {
    val row = new Row()
    row.add(cell('A'))
    row.add(cell('B'))
    row.add(cell('C'))
    val out = renderToString(row)
    assertEquals(out, reset + moveTo(1, 1) + "A" + "B" + "C" + reset)
  }

  test("Row respects child width when advancing x offset") {
    val row = new Row()
    row.add(filledCell('A', width = 3, height = 1))
    row.add(filledCell('B', width = 3, height = 1))
    // 'A' fills cols 1-3, 'B' fills cols 4-6 (adjacent runs — no extra moveTo)
    val out = renderToString(row)
    assertEquals(out, reset + moveTo(1, 1) + "AAA" + "BBB" + reset)
  }

  // ---------------------------------------------------------------------------
  // Column — size accumulation
  // ---------------------------------------------------------------------------

  test("Column size is zero with no children") {
    val col = new Column()
    assertEquals(col.size, Size(0, 0))
  }

  test("Column size takes max width and sums heights") {
    val col = new Column()
    col.add(cell('A', width = 3, height = 1))
    col.add(cell('B', width = 5, height = 2))
    // width = max(3, 5) = 5, height = 1 + 2 = 3
    assertEquals(col.size, Size(5, 3))
  }

  // ---------------------------------------------------------------------------
  // Column — layout
  // ---------------------------------------------------------------------------

  test("Column places a single child at the origin") {
    val col = new Column()
    col.add(cell('A'))
    val out = renderToString(col)
    assertEquals(out, reset + moveTo(1, 1) + "A" + reset)
  }

  test("Column places two children top to bottom") {
    val col = new Column()
    col.add(cell('A'))
    col.add(cell('B'))
    // 'A' at row 1, 'B' at row 2
    val out = renderToString(col)
    assertEquals(out, reset + moveTo(1, 1) + "A" + moveTo(1, 2) + "B" + reset)
  }

  test("Column places three children top to bottom") {
    val col = new Column()
    col.add(cell('A'))
    col.add(cell('B'))
    col.add(cell('C'))
    val out = renderToString(col)
    assertEquals(
      out,
      reset + moveTo(1, 1) + "A" + moveTo(1, 2) + "B" + moveTo(
        1,
        3
      ) + "C" + reset
    )
  }

  test("Column respects child height when advancing y offset") {
    val col = new Column()
    col.add(filledCell('A', width = 1, height = 3))
    col.add(filledCell('B', width = 1, height = 3))
    // 'A' fills rows 1-3, 'B' fills rows 4-6
    val out = renderToString(col)
    assertEquals(
      out,
      reset +
        moveTo(1, 1) + "A" +
        moveTo(1, 2) + "A" +
        moveTo(1, 3) + "A" +
        moveTo(1, 4) + "B" +
        moveTo(1, 5) + "B" +
        moveTo(1, 6) + "B" +
        reset
    )
  }

  // ---------------------------------------------------------------------------
  // Nesting — Row inside Column and Column inside Row
  // ---------------------------------------------------------------------------

  test("Column containing a Row: children laid out correctly") {
    // Column: Row("A","B") on top, Row("C","D") below
    val col = new Column()
    val top = new Row()
    top.add(cell('A'))
    top.add(cell('B'))
    val bot = new Row()
    bot.add(cell('C'))
    bot.add(cell('D'))
    col.add(top)
    col.add(bot)
    // Buffer is 2 wide x 2 tall
    // top row at y=0: 'A' at (0,0), 'B' at (1,0)
    // bot row at y=1: 'C' at (0,1), 'D' at (1,1)
    val out = renderToString(col)
    assertEquals(
      out,
      reset + moveTo(1, 1) + "A" + "B" + moveTo(1, 2) + "C" + "D" + reset
    )
  }

  test("Row containing a Column: children laid out correctly") {
    // Row: Column("A","C") on left, Column("B","D") on right
    val row = new Row()
    val left = new Column()
    left.add(cell('A'))
    left.add(cell('C'))
    val right = new Column()
    right.add(cell('B'))
    right.add(cell('D'))
    row.add(left)
    row.add(right)
    // Buffer is 2 wide x 2 tall
    // left column at x=0: 'A' at (0,0), 'C' at (0,1)
    // right column at x=1: 'B' at (1,0), 'D' at (1,1)
    val out = renderToString(row)
    assertEquals(
      out,
      reset + moveTo(1, 1) + "A" + "B" + moveTo(1, 2) + "C" + "D" + reset
    )
  }
