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

import munit.FunSuite
import terminus.StringBuilderTerminal
import terminus.effect.AnsiCodes
import terminus.ui.Buffer
import terminus.ui.Cell
import terminus.ui.Component
import terminus.ui.Constraint
import terminus.ui.Rect
import terminus.ui.Size
import terminus.ui.style.Align
import terminus.ui.style.CellStyle
import terminus.ui.style.Justify
import terminus.ui.style.LayoutStyle

class LayoutSuite extends FunSuite:

  /** A minimal component that writes a single character at its origin. */
  def cell(char: Char, width: Int = 1, height: Int = 1): Component =
    new Component:
      val size: Size = Size.fixed(width, height)
      def render(bounds: Rect, buf: Buffer): Unit =
        buf.put(bounds.x, bounds.y, Cell(char.toInt, CellStyle.default))

  /** A component that fills its entire allocated bounds with a single
    * character.
    */
  def filledCell(char: Char, sz: Size = Size.fixed(1, 1)): Component =
    new Component:
      val size: Size = sz
      def render(bounds: Rect, buf: Buffer): Unit =
        val c = Cell(char.toInt, CellStyle.default)
        var y = bounds.y
        while y < bounds.bottom do
          var x = bounds.x
          while x < bounds.right do
            buf.put(x, y, c)
            x += 1
          y += 1

  /** Render a component into a buffer of the given size and return terminal
    * output.
    */
  def renderToString(component: Component, width: Int, height: Int): String =
    val buf = Buffer(width, height)
    component.render(Rect(0, 0, width, height), buf)
    val t = StringBuilderTerminal()
    buf.render(using t)
    t.result()

  /** Render a fixed-size component into a buffer matching its size. */
  def renderToString(component: Component): String =
    val dims = component.size.toDimensions
    renderToString(component, dims.width, dims.height)

  val reset: String = AnsiCodes.sgr("0")
  def moveTo(col: Int, row: Int): String = AnsiCodes.cursor.to(col, row)

  // ---------------------------------------------------------------------------
  // Row — fixed children
  // ---------------------------------------------------------------------------

  test("Row places a single child at the origin") {
    val row = new Row(Size.fixed(1, 1))
    row.add(cell('A'))
    val out = renderToString(row)
    assertEquals(out, reset + moveTo(1, 1) + "A" + reset)
  }

  test("Row places two fixed children left to right") {
    val row = new Row(Size.fixed(2, 1))
    row.add(cell('A'))
    row.add(cell('B'))
    val out = renderToString(row)
    assertEquals(out, reset + moveTo(1, 1) + "A" + "B" + reset)
  }

  test("Row places three fixed children left to right") {
    val row = new Row(Size.fixed(3, 1))
    row.add(cell('A'))
    row.add(cell('B'))
    row.add(cell('C'))
    val out = renderToString(row)
    assertEquals(out, reset + moveTo(1, 1) + "A" + "B" + "C" + reset)
  }

  test("Row respects child width when advancing x offset") {
    val row = new Row(Size.fixed(6, 1))
    row.add(filledCell('A', Size.fixed(3, 1)))
    row.add(filledCell('B', Size.fixed(3, 1)))
    val out = renderToString(row)
    assertEquals(out, reset + moveTo(1, 1) + "AAA" + "BBB" + reset)
  }

  // ---------------------------------------------------------------------------
  // Row — weight children
  // ---------------------------------------------------------------------------

  test("Row distributes remaining width equally to equal-weight children") {
    val row = new Row(Size.fixed(6, 1))
    row.add(filledCell('A', Size(Constraint.Weight(1), Constraint.Fixed(1))))
    row.add(filledCell('B', Size(Constraint.Weight(1), Constraint.Fixed(1))))
    row.add(filledCell('C', Size(Constraint.Weight(1), Constraint.Fixed(1))))
    val out = renderToString(row)
    assertEquals(out, reset + moveTo(1, 1) + "AA" + "BB" + "CC" + reset)
  }

  test("Row gives remaining width after fixed child to weight child") {
    val row = new Row(Size.fixed(6, 1))
    row.add(filledCell('A', Size.fixed(2, 1)))
    row.add(filledCell('B', Size(Constraint.Weight(1), Constraint.Fixed(1))))
    val out = renderToString(row)
    assertEquals(out, reset + moveTo(1, 1) + "AA" + "BBBB" + reset)
  }

  // ---------------------------------------------------------------------------
  // Column — fixed children
  // ---------------------------------------------------------------------------

  test("Column places a single child at the origin") {
    val col = new Column(Size.fixed(1, 1))
    col.add(cell('A'))
    val out = renderToString(col)
    assertEquals(out, reset + moveTo(1, 1) + "A" + reset)
  }

  test("Column places two fixed children top to bottom") {
    val col = new Column(Size.fixed(1, 2))
    col.add(cell('A'))
    col.add(cell('B'))
    val out = renderToString(col)
    assertEquals(out, reset + moveTo(1, 1) + "A" + moveTo(1, 2) + "B" + reset)
  }

  test("Column places three fixed children top to bottom") {
    val col = new Column(Size.fixed(1, 3))
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
    val col = new Column(Size.fixed(1, 6))
    col.add(filledCell('A', Size.fixed(1, 3)))
    col.add(filledCell('B', Size.fixed(1, 3)))
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
  // Column — weight children
  // ---------------------------------------------------------------------------

  test("Column distributes remaining height equally to equal-weight children") {
    val col = new Column(Size.fixed(1, 6))
    col.add(filledCell('A', Size(Constraint.Fixed(1), Constraint.Weight(1))))
    col.add(filledCell('B', Size(Constraint.Fixed(1), Constraint.Weight(1))))
    col.add(filledCell('C', Size(Constraint.Fixed(1), Constraint.Weight(1))))
    val out = renderToString(col)
    assertEquals(
      out,
      reset +
        moveTo(1, 1) + "A" +
        moveTo(1, 2) + "A" +
        moveTo(1, 3) + "B" +
        moveTo(1, 4) + "B" +
        moveTo(1, 5) + "C" +
        moveTo(1, 6) + "C" +
        reset
    )
  }

  test("Column gives remaining height after fixed child to weight child") {
    val col = new Column(Size.fixed(1, 6))
    col.add(filledCell('A', Size.fixed(1, 2)))
    col.add(filledCell('B', Size(Constraint.Fixed(1), Constraint.Weight(1))))
    val out = renderToString(col)
    assertEquals(
      out,
      reset +
        moveTo(1, 1) + "A" +
        moveTo(1, 2) + "A" +
        moveTo(1, 3) + "B" +
        moveTo(1, 4) + "B" +
        moveTo(1, 5) + "B" +
        moveTo(1, 6) + "B" +
        reset
    )
  }

  // ---------------------------------------------------------------------------
  // Row — percentage children
  // ---------------------------------------------------------------------------

  test("Row: percentage(1.0) fills all space remaining after fixed child") {
    // 10 wide, fixed child takes 2, percentage(1.0) gets remaining 8
    val row = new Row(Size.fixed(10, 1))
    row.add(filledCell('A', Size.fixed(2, 1)))
    row.add(
      filledCell('B', Size(Constraint.Percentage(1.0), Constraint.Fixed(1)))
    )
    val out = renderToString(row)
    assertEquals(out, reset + moveTo(1, 1) + "AA" + "BBBBBBBB" + reset)
  }

  test("Row: fixed, then percentage, then weight share space in three phases") {
    // 10 wide: fixed takes 2, percentage(0.5) gets 50% of remaining 8 = 4, weight gets last 4
    val row = new Row(Size.fixed(10, 1))
    row.add(filledCell('A', Size.fixed(2, 1)))
    row.add(
      filledCell('B', Size(Constraint.Percentage(0.5), Constraint.Fixed(1)))
    )
    row.add(filledCell('C', Size(Constraint.Weight(1), Constraint.Fixed(1))))
    val out = renderToString(row)
    assertEquals(out, reset + moveTo(1, 1) + "AA" + "BBBB" + "CCCC" + reset)
  }

  // ---------------------------------------------------------------------------
  // Column — percentage children
  // ---------------------------------------------------------------------------

  test(
    "Column: fixed, then percentage, then weight share space in three phases"
  ) {
    // 10 tall: fixed takes 2, percentage(0.5) gets 50% of remaining 8 = 4, weight gets last 4
    val col = new Column(Size.fixed(1, 10))
    col.add(filledCell('A', Size.fixed(1, 2)))
    col.add(
      filledCell('B', Size(Constraint.Fixed(1), Constraint.Percentage(0.5)))
    )
    col.add(filledCell('C', Size(Constraint.Fixed(1), Constraint.Weight(1))))
    val out = renderToString(col)
    assertEquals(
      out,
      reset +
        moveTo(1, 1) + "A" + moveTo(1, 2) + "A" +
        moveTo(1, 3) + "B" + moveTo(1, 4) + "B" +
        moveTo(1, 5) + "B" + moveTo(1, 6) + "B" +
        moveTo(1, 7) + "C" + moveTo(1, 8) + "C" +
        moveTo(1, 9) + "C" + moveTo(1, 10) + "C" +
        reset
    )
  }

  // ---------------------------------------------------------------------------
  // Nesting — Row inside Column and Column inside Row
  // ---------------------------------------------------------------------------

  test("Column containing a Row: children laid out correctly") {
    val col = new Column(Size.fixed(2, 2))
    val top = new Row(Size.fixed(2, 1))
    top.add(cell('A'))
    top.add(cell('B'))
    val bot = new Row(Size.fixed(2, 1))
    bot.add(cell('C'))
    bot.add(cell('D'))
    col.add(top)
    col.add(bot)
    val out = renderToString(col)
    assertEquals(
      out,
      reset + moveTo(1, 1) + "A" + "B" + moveTo(1, 2) + "C" + "D" + reset
    )
  }

  test("Row containing a Column: children laid out correctly") {
    val row = new Row(Size.fixed(2, 2))
    val left = new Column(Size.fixed(1, 2))
    left.add(cell('A'))
    left.add(cell('C'))
    val right = new Column(Size.fixed(1, 2))
    right.add(cell('B'))
    right.add(cell('D'))
    row.add(left)
    row.add(right)
    val out = renderToString(row)
    assertEquals(
      out,
      reset + moveTo(1, 1) + "A" + "B" + moveTo(1, 2) + "C" + "D" + reset
    )
  }

  // ---------------------------------------------------------------------------
  // Row — justify-content
  // Buffer renders ALL cells (Cell.empty = space), so expected includes spaces.
  // ---------------------------------------------------------------------------

  test("Row Justify.End packs children at the right") {
    // 6 wide, 2 × filledCell(2,1) → freeSpace=2, startOffset=2
    // cells: "  AABB"
    val row = new Row(Size.fixed(6, 1), LayoutStyle(justify = Justify.End))
    row.add(filledCell('A', Size.fixed(2, 1)))
    row.add(filledCell('B', Size.fixed(2, 1)))
    val out = renderToString(row)
    assertEquals(out, reset + moveTo(1, 1) + "  AABB" + reset)
  }

  test("Row Justify.Center centers children") {
    // 6 wide, 2 × filledCell(1,1) → freeSpace=4, startOffset=2
    // cells: "  AB  "
    val row = new Row(Size.fixed(6, 1), LayoutStyle(justify = Justify.Center))
    row.add(filledCell('A', Size.fixed(1, 1)))
    row.add(filledCell('B', Size.fixed(1, 1)))
    val out = renderToString(row)
    assertEquals(out, reset + moveTo(1, 1) + "  AB  " + reset)
  }

  test("Row Justify.SpaceBetween distributes space between children") {
    // 9 wide, 3 × filledCell(1,1) → freeSpace=6, gap=3
    // cells: "A   B   C"
    val row =
      new Row(Size.fixed(9, 1), LayoutStyle(justify = Justify.SpaceBetween))
    row.add(filledCell('A', Size.fixed(1, 1)))
    row.add(filledCell('B', Size.fixed(1, 1)))
    row.add(filledCell('C', Size.fixed(1, 1)))
    val out = renderToString(row)
    assertEquals(out, reset + moveTo(1, 1) + "A   B   C" + reset)
  }

  test("Row Justify.SpaceEvenly distributes space evenly including edges") {
    // 8 wide, 2 × filledCell(1,1) → freeSpace=6, n+1=3 gaps, gap=2
    // cells: "  A  B  "
    val row =
      new Row(Size.fixed(8, 1), LayoutStyle(justify = Justify.SpaceEvenly))
    row.add(filledCell('A', Size.fixed(1, 1)))
    row.add(filledCell('B', Size.fixed(1, 1)))
    val out = renderToString(row)
    assertEquals(out, reset + moveTo(1, 1) + "  A  B  " + reset)
  }

  // ---------------------------------------------------------------------------
  // Row — align-items
  // ---------------------------------------------------------------------------

  test("Row Align.Start positions child at top of cross axis") {
    // Row 1 wide, 3 tall. Child 1x1. Start → y=0, rows 1-2 empty.
    val row = new Row(Size.fixed(1, 3), LayoutStyle(align = Align.Start))
    row.add(cell('A'))
    val out = renderToString(row)
    assertEquals(
      out,
      reset + moveTo(1, 1) + "A" + moveTo(1, 2) + " " + moveTo(
        1,
        3
      ) + " " + reset
    )
  }

  test("Row Align.Center positions child in middle of cross axis") {
    // Row 1 wide, 3 tall. Child 1x1. Center → y=(3-1)/2=1.
    val row = new Row(Size.fixed(1, 3), LayoutStyle(align = Align.Center))
    row.add(cell('A'))
    val out = renderToString(row)
    assertEquals(
      out,
      reset + moveTo(1, 1) + " " + moveTo(1, 2) + "A" + moveTo(
        1,
        3
      ) + " " + reset
    )
  }

  test("Row Align.End positions child at bottom of cross axis") {
    // Row 1 wide, 3 tall. Child 1x1. End → y=3-1=2.
    val row = new Row(Size.fixed(1, 3), LayoutStyle(align = Align.End))
    row.add(cell('A'))
    val out = renderToString(row)
    assertEquals(
      out,
      reset + moveTo(1, 1) + " " + moveTo(1, 2) + " " + moveTo(
        1,
        3
      ) + "A" + reset
    )
  }

  // ---------------------------------------------------------------------------
  // Column — justify-content
  // ---------------------------------------------------------------------------

  test("Column Justify.End packs children at the bottom") {
    // 6 tall, 2 × cell(1 tall) → freeSpace=4, startOffset=4
    // rows 1-4 empty, rows 5-6 have A/B
    val col = new Column(Size.fixed(1, 6), LayoutStyle(justify = Justify.End))
    col.add(cell('A'))
    col.add(cell('B'))
    val out = renderToString(col)
    assertEquals(
      out,
      reset +
        moveTo(1, 1) + " " + moveTo(1, 2) + " " + moveTo(1, 3) + " " + moveTo(
          1,
          4
        ) + " " +
        moveTo(1, 5) + "A" + moveTo(1, 6) + "B" +
        reset
    )
  }

  test("Column Justify.Center centers children") {
    // 6 tall, 2 × cell(1 tall) → freeSpace=4, startOffset=2
    // rows 1-2 empty, rows 3-4 have A/B, rows 5-6 empty
    val col =
      new Column(Size.fixed(1, 6), LayoutStyle(justify = Justify.Center))
    col.add(cell('A'))
    col.add(cell('B'))
    val out = renderToString(col)
    assertEquals(
      out,
      reset +
        moveTo(1, 1) + " " + moveTo(1, 2) + " " +
        moveTo(1, 3) + "A" + moveTo(1, 4) + "B" +
        moveTo(1, 5) + " " + moveTo(1, 6) + " " +
        reset
    )
  }

  // ---------------------------------------------------------------------------
  // Column — align-items
  // ---------------------------------------------------------------------------

  test("Column Align.Center positions child in middle of cross axis") {
    // Column 3 wide, 1 tall. Child 1x1. Center → x=(3-1)/2=1. cells: " A "
    val col = new Column(Size.fixed(3, 1), LayoutStyle(align = Align.Center))
    col.add(cell('A'))
    val out = renderToString(col)
    assertEquals(out, reset + moveTo(1, 1) + " A " + reset)
  }

  test("Column Align.End positions child at right of cross axis") {
    // Column 3 wide, 1 tall. Child 1x1. End → x=3-1=2. cells: "  A"
    val col = new Column(Size.fixed(3, 1), LayoutStyle(align = Align.End))
    col.add(cell('A'))
    val out = renderToString(col)
    assertEquals(out, reset + moveTo(1, 1) + "  A" + reset)
  }
