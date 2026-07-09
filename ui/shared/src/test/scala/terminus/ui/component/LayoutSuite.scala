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
import terminus.ui.capability.React
import terminus.ui.event.DefaultEvent
import terminus.ui.event.FocusId
import terminus.ui.layout.Buffer
import terminus.ui.layout.Cell
import terminus.ui.layout.CellArrayBuffer
import terminus.ui.layout.Component
import terminus.ui.layout.Constraint
import terminus.ui.layout.DefaultLayout
import terminus.ui.layout.Dimensions
import terminus.ui.layout.Infinity
import terminus.ui.layout.Measurement
import terminus.ui.layout.Rect
import terminus.ui.layout.Size
import terminus.ui.runtime.Runtime
import terminus.ui.style.Align
import terminus.ui.style.CellProps
import terminus.ui.style.Justify
import terminus.ui.style.LayoutProps

class LayoutSuite extends FunSuite:

  /** A leaf component with the given size. Its natural dimensions are the fixed
    * cells on each axis (zero on a flexible axis), and it draws via `draw`.
    */
  def component(sz: Size)(draw: (Rect, Buffer) => Unit): Component =
    new Component:
      val size: Size = sz
      private val natWidth = sz.width match
        case Measurement.Fixed(cells) => cells
        case _                        => 0
      private val natHeight = sz.height match
        case Measurement.Fixed(cells) => cells
        case _                        => 0

      def react(using React): Unit = ()
      def measure(constraint: Constraint): Dimensions =
        constraint.constrain(Dimensions(natWidth, natHeight))
      def minIntrinsicWidth(height: Int | Infinity): Int = natWidth
      def maxIntrinsicWidth(height: Int | Infinity): Int = natWidth
      def minIntrinsicHeight(width: Int | Infinity): Int = natHeight
      def maxIntrinsicHeight(width: Int | Infinity): Int = natHeight
      def render(dimensions: Dimensions, buf: Buffer): Unit =
        // The component draws from its own origin into the view it is given, so
        // the bounds handed to `draw` are anchored at (0, 0).
        draw(Rect(0, 0, dimensions.width, dimensions.height), buf)

  /** A minimal component that writes a single character at its origin. */
  def cell(char: Char, width: Int = 1, height: Int = 1): Component =
    component(Size.fixed(width, height)) { (bounds, buf) =>
      buf.put(bounds.x, bounds.y, Cell(char.toInt, CellProps.default))
    }

  /** A component that fills its entire allocated bounds with a single
    * character.
    */
  def filledCell(char: Char, sz: Size = Size.fixed(1, 1)): Component =
    component(sz) { (bounds, buf) =>
      val c = Cell(char.toInt, CellProps.default)
      var y = bounds.y
      while y < bounds.bottom do
        var x = bounds.x
        while x < bounds.right do
          buf.put(x, y, c)
          x += 1
        y += 1
    }

  /** Build a context populated with the given child components, as a parent
    * layout container would after evaluating its body.
    */
  def contextOf(children: Component*): DefaultEvent & DefaultLayout =
    val runtime = Runtime.empty
    val ctx =
      new DefaultEvent(FocusId.next, runtime) with DefaultLayout(runtime) {}
    children.foreach(child => ctx.addComponent(_ => child))
    ctx

  /** Construct a [[Row]] containing the given children. */
  def row(size: Size, style: LayoutProps = LayoutProps.default)(
      children: Component*
  ): Row =
    new Row(size, style, contextOf(children*))

  /** Construct a [[Column]] containing the given children. */
  def column(size: Size, style: LayoutProps = LayoutProps.default)(
      children: Component*
  ): Column =
    new Column(size, style, contextOf(children*))

  /** Render a component into a buffer of the given size and return terminal
    * output.
    */
  def renderToString(component: Component, width: Int, height: Int): String =
    val buf = CellArrayBuffer(width, height)
    component.render(Dimensions(width, height), buf)
    val t = StringBuilderTerminal()
    buf.render(using t)
    t.result()

  /** Render a fixed-size component into a buffer matching its size. */
  def renderToString(component: Component): String =
    val dims = component.measure(Constraint.loose(Infinity, Infinity))
    renderToString(component, dims.width, dims.height)

  val reset: String = AnsiCodes.sgr("0")
  def moveTo(col: Int, row: Int): String = AnsiCodes.cursor.to(col, row)

  // ---------------------------------------------------------------------------
  // Row — fixed children
  // ---------------------------------------------------------------------------

  test("Row places a single child at the origin") {
    val r = row(Size.fixed(1, 1))(cell('A'))
    val out = renderToString(r)
    assertEquals(out, reset + moveTo(1, 1) + "A" + reset)
  }

  test("Row places two fixed children left to right") {
    val r = row(Size.fixed(2, 1))(cell('A'), cell('B'))
    val out = renderToString(r)
    assertEquals(out, reset + moveTo(1, 1) + "A" + "B" + reset)
  }

  test("Row places three fixed children left to right") {
    val r = row(Size.fixed(3, 1))(cell('A'), cell('B'), cell('C'))
    val out = renderToString(r)
    assertEquals(out, reset + moveTo(1, 1) + "A" + "B" + "C" + reset)
  }

  test("Row respects child width when advancing x offset") {
    val r = row(Size.fixed(6, 1))(
      filledCell('A', Size.fixed(3, 1)),
      filledCell('B', Size.fixed(3, 1))
    )
    val out = renderToString(r)
    assertEquals(out, reset + moveTo(1, 1) + "AAA" + "BBB" + reset)
  }

  // ---------------------------------------------------------------------------
  // Row — weight children
  // ---------------------------------------------------------------------------

  test("Row distributes remaining width equally to equal-weight children") {
    val r = row(Size.fixed(6, 1))(
      filledCell('A', Size(Measurement.Weight(1), Measurement.Fixed(1))),
      filledCell('B', Size(Measurement.Weight(1), Measurement.Fixed(1))),
      filledCell('C', Size(Measurement.Weight(1), Measurement.Fixed(1)))
    )
    val out = renderToString(r)
    assertEquals(out, reset + moveTo(1, 1) + "AA" + "BB" + "CC" + reset)
  }

  test("Row gives remaining width after fixed child to weight child") {
    val r = row(Size.fixed(6, 1))(
      filledCell('A', Size.fixed(2, 1)),
      filledCell('B', Size(Measurement.Weight(1), Measurement.Fixed(1)))
    )
    val out = renderToString(r)
    assertEquals(out, reset + moveTo(1, 1) + "AA" + "BBBB" + reset)
  }

  // ---------------------------------------------------------------------------
  // Column — fixed children
  // ---------------------------------------------------------------------------

  test("Column places a single child at the origin") {
    val col = column(Size.fixed(1, 1))(cell('A'))
    val out = renderToString(col)
    assertEquals(out, reset + moveTo(1, 1) + "A" + reset)
  }

  test("Column places two fixed children top to bottom") {
    val col = column(Size.fixed(1, 2))(cell('A'), cell('B'))
    val out = renderToString(col)
    assertEquals(out, reset + moveTo(1, 1) + "A" + moveTo(1, 2) + "B" + reset)
  }

  test("Column places three fixed children top to bottom") {
    val col = column(Size.fixed(1, 3))(cell('A'), cell('B'), cell('C'))
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
    val col = column(Size.fixed(1, 6))(
      filledCell('A', Size.fixed(1, 3)),
      filledCell('B', Size.fixed(1, 3))
    )
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
    val col = column(Size.fixed(1, 6))(
      filledCell('A', Size(Measurement.Fixed(1), Measurement.Weight(1))),
      filledCell('B', Size(Measurement.Fixed(1), Measurement.Weight(1))),
      filledCell('C', Size(Measurement.Fixed(1), Measurement.Weight(1)))
    )
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
    val col = column(Size.fixed(1, 6))(
      filledCell('A', Size.fixed(1, 2)),
      filledCell('B', Size(Measurement.Fixed(1), Measurement.Weight(1)))
    )
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
    val r = row(Size.fixed(10, 1))(
      filledCell('A', Size.fixed(2, 1)),
      filledCell('B', Size(Measurement.Percentage(1.0), Measurement.Fixed(1)))
    )
    val out = renderToString(r)
    assertEquals(out, reset + moveTo(1, 1) + "AA" + "BBBBBBBB" + reset)
  }

  test("Row: fixed, then percentage, then weight share space in three phases") {
    // 10 wide: fixed takes 2, percentage(0.5) gets 50% of remaining 8 = 4, weight gets last 4
    val r = row(Size.fixed(10, 1))(
      filledCell('A', Size.fixed(2, 1)),
      filledCell('B', Size(Measurement.Percentage(0.5), Measurement.Fixed(1))),
      filledCell('C', Size(Measurement.Weight(1), Measurement.Fixed(1)))
    )
    val out = renderToString(r)
    assertEquals(out, reset + moveTo(1, 1) + "AA" + "BBBB" + "CCCC" + reset)
  }

  // ---------------------------------------------------------------------------
  // Column — percentage children
  // ---------------------------------------------------------------------------

  test(
    "Column: fixed, then percentage, then weight share space in three phases"
  ) {
    // 10 tall: fixed takes 2, percentage(0.5) gets 50% of remaining 8 = 4, weight gets last 4
    val col = column(Size.fixed(1, 10))(
      filledCell('A', Size.fixed(1, 2)),
      filledCell('B', Size(Measurement.Fixed(1), Measurement.Percentage(0.5))),
      filledCell('C', Size(Measurement.Fixed(1), Measurement.Weight(1)))
    )
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
    val col = column(Size.fixed(2, 2))(
      row(Size.fixed(2, 1))(cell('A'), cell('B')),
      row(Size.fixed(2, 1))(cell('C'), cell('D'))
    )
    val out = renderToString(col)
    assertEquals(
      out,
      reset + moveTo(1, 1) + "A" + "B" + moveTo(1, 2) + "C" + "D" + reset
    )
  }

  test("Row containing a Column: children laid out correctly") {
    val r = row(Size.fixed(2, 2))(
      column(Size.fixed(1, 2))(cell('A'), cell('C')),
      column(Size.fixed(1, 2))(cell('B'), cell('D'))
    )
    val out = renderToString(r)
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
    val r = row(Size.fixed(6, 1), LayoutProps(justify = Justify.End))(
      filledCell('A', Size.fixed(2, 1)),
      filledCell('B', Size.fixed(2, 1))
    )
    val out = renderToString(r)
    assertEquals(out, reset + moveTo(1, 1) + "  AABB" + reset)
  }

  test("Row Justify.Center centers children") {
    // 6 wide, 2 × filledCell(1,1) → freeSpace=4, startOffset=2
    // cells: "  AB  "
    val r = row(Size.fixed(6, 1), LayoutProps(justify = Justify.Center))(
      filledCell('A', Size.fixed(1, 1)),
      filledCell('B', Size.fixed(1, 1))
    )
    val out = renderToString(r)
    assertEquals(out, reset + moveTo(1, 1) + "  AB  " + reset)
  }

  test("Row Justify.SpaceBetween distributes space between children") {
    // 9 wide, 3 × filledCell(1,1) → freeSpace=6, gap=3
    // cells: "A   B   C"
    val r = row(Size.fixed(9, 1), LayoutProps(justify = Justify.SpaceBetween))(
      filledCell('A', Size.fixed(1, 1)),
      filledCell('B', Size.fixed(1, 1)),
      filledCell('C', Size.fixed(1, 1))
    )
    val out = renderToString(r)
    assertEquals(out, reset + moveTo(1, 1) + "A   B   C" + reset)
  }

  test("Row Justify.SpaceEvenly distributes space evenly including edges") {
    // 8 wide, 2 × filledCell(1,1) → freeSpace=6, n+1=3 gaps, gap=2
    // cells: "  A  B  "
    val r = row(Size.fixed(8, 1), LayoutProps(justify = Justify.SpaceEvenly))(
      filledCell('A', Size.fixed(1, 1)),
      filledCell('B', Size.fixed(1, 1))
    )
    val out = renderToString(r)
    assertEquals(out, reset + moveTo(1, 1) + "  A  B  " + reset)
  }

  // ---------------------------------------------------------------------------
  // Row — align-items
  // ---------------------------------------------------------------------------

  test("Row Align.Start positions child at top of cross axis") {
    // Row 1 wide, 3 tall. Child 1x1. Start → y=0, rows 1-2 empty.
    val r = row(Size.fixed(1, 3), LayoutProps(align = Align.Start))(cell('A'))
    val out = renderToString(r)
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
    val r = row(Size.fixed(1, 3), LayoutProps(align = Align.Center))(cell('A'))
    val out = renderToString(r)
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
    val r = row(Size.fixed(1, 3), LayoutProps(align = Align.End))(cell('A'))
    val out = renderToString(r)
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
    val col = column(Size.fixed(1, 6), LayoutProps(justify = Justify.End))(
      cell('A'),
      cell('B')
    )
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
    val col = column(Size.fixed(1, 6), LayoutProps(justify = Justify.Center))(
      cell('A'),
      cell('B')
    )
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
    val col = column(Size.fixed(3, 1), LayoutProps(align = Align.Center))(
      cell('A')
    )
    val out = renderToString(col)
    assertEquals(out, reset + moveTo(1, 1) + " A " + reset)
  }

  test("Column Align.End positions child at right of cross axis") {
    // Column 3 wide, 1 tall. Child 1x1. End → x=3-1=2. cells: "  A"
    val col =
      column(Size.fixed(3, 1), LayoutProps(align = Align.End))(cell('A'))
    val out = renderToString(col)
    assertEquals(out, reset + moveTo(1, 1) + "  A" + reset)
  }
