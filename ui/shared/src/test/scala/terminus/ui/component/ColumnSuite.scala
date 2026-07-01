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
import terminus.ui.style.CellStyle
import terminus.ui.style.Justify
import terminus.ui.style.LayoutStyle

/** Tests focused on [[Column]] layout. Components now render from their own
  * origin into a view the parent clips to their slot, so a child cannot observe
  * its absolute position directly. Instead each child fills its view with a
  * unique mark, and we scan the backing buffer to recover where it actually
  * drew ([[cellsOf]] / [[rectOf]]). That keeps the two failure modes the user
  * reported directly observable:
  *
  *   - **overlap** — a child fails to paint its full area because a sibling
  *     overwrote part of it (caught by [[assertNoOverlap]]), and
  *   - **overflow** — a child paints outside the column's bounds. This is now
  *     structurally impossible (the view clips it), so the tests assert that
  *     the clipping actually happens: an over-subscribed child is trimmed or
  *     drawn not at all rather than spilling onto a sibling.
  */
class ColumnSuite extends FunSuite:

  /** A leaf component with a unique [[mark]]. When rendered it fills the view
    * it is given with that mark and remembers the [[Dimensions]] it was asked
    * to draw, so a scan of the backing buffer reveals its actual (clipped)
    * placement.
    */
  final class Recorder(val size: Size, val mark: Int) extends Component:
    /** The Dimensions this component was last rendered at, if ever. */
    var rendered: Option[Dimensions] = None

    private val natWidth = size.width match
      case Measurement.Fixed(cells) => cells
      case _                        => 0
    private val natHeight = size.height match
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
      rendered = Some(dimensions)
      buf.fill(
        Rect(0, 0, dimensions.width, dimensions.height),
        Cell(mark, CellStyle.default)
      )

  // Each recorder gets a distinct code point so its cells are identifiable in
  // the buffer. Starts above the space/continuation sentinels and never reaches
  // them.
  private var nextMark = 'A'.toInt
  def recorder(size: Size): Recorder =
    val r = new Recorder(size, nextMark)
    nextMark += 1
    r
  def fixed(w: Int, h: Int): Recorder = recorder(Size.fixed(w, h))
  def weight(w: Int, crossWidth: Int = 1): Recorder =
    recorder(Size(Measurement.Fixed(crossWidth), Measurement.Weight(w)))
  def percent(p: Double, crossWidth: Int = 1): Recorder =
    recorder(Size(Measurement.Fixed(crossWidth), Measurement.Percentage(p)))

  def contextOf(children: Component*): DefaultEvent & DefaultLayout =
    val runtime = Runtime.empty
    val ctx =
      new DefaultEvent(FocusId.next, runtime) with DefaultLayout(runtime) {}
    children.foreach(child => ctx.addComponent(_ => child))
    ctx

  def column(size: Size, style: LayoutStyle = LayoutStyle.default)(
      children: Component*
  ): Column =
    new Column(size, style, contextOf(children*))

  /** Render `col` at `at` into a fresh buffer large enough to hold it, through
    * a view positioned at `at` — mirroring how a parent hands a child a
    * clipped, origin-relative buffer. Returns the buffer for inspection.
    */
  def render(col: Column, at: Rect): CellArrayBuffer =
    val buf = CellArrayBuffer(at.right.max(0), at.bottom.max(0))
    col.render(Dimensions(at.width, at.height), buf.view(at))
    buf

  /** The set of absolute cells painted with `mark`. */
  def cellsOf(buf: CellArrayBuffer, mark: Int): Set[(Int, Int)] =
    val out = Set.newBuilder[(Int, Int)]
    var y = 0
    while y < buf.height do
      var x = 0
      while x < buf.width do
        if buf.get(x, y).codePoint == mark then out += ((x, y))
        x += 1
      y += 1
    out.result()

  /** The bounding rect of `mark`'s painted cells, or None if it painted none.
    */
  def rectOf(buf: CellArrayBuffer, mark: Int): Option[Rect] =
    val cells = cellsOf(buf, mark)
    if cells.isEmpty then None
    else
      val xs = cells.map(_._1)
      val ys = cells.map(_._2)
      Some(Rect(xs.min, ys.min, xs.max - xs.min + 1, ys.max - ys.min + 1))

  /** Assert `r` painted a solid rectangle of exactly the size it was rendered
    * at — neither clipped nor partly overwritten by a sibling.
    */
  def assertFullyPainted(buf: CellArrayBuffer, r: Recorder): Unit =
    val expected =
      r.rendered.getOrElse(
        fail(s"recorder ${r.mark.toChar} was never rendered")
      )
    assertEquals(
      cellsOf(buf, r.mark).size,
      expected.width * expected.height,
      s"recorder ${r.mark.toChar} did not paint its full area " +
        s"(clipped or overlapped by a sibling)"
    )

  /** Assert no two recorders share a cell. Because the buffer keeps only the
    * last writer, an overlap shows up as an earlier child failing to paint its
    * full area, so this reduces to every recorder being fully painted.
    */
  def assertNoOverlap(buf: CellArrayBuffer, rs: Recorder*): Unit =
    rs.foreach(r => assertFullyPainted(buf, r))

  /** Assert every cell any recorder painted lies within `bounds`. */
  def assertWithin(bounds: Rect, buf: CellArrayBuffer, rs: Recorder*): Unit =
    rs.foreach { r =>
      cellsOf(buf, r.mark).foreach { (x, y) =>
        assert(
          x >= bounds.x && x < bounds.right &&
            y >= bounds.y && y < bounds.bottom,
          s"recorder ${r.mark.toChar} painted ($x, $y) outside $bounds"
        )
      }
    }

  // ---------------------------------------------------------------------------
  // Fixed children — exact stacking
  // ---------------------------------------------------------------------------

  test("fixed children stack top to bottom without overlap") {
    val a, b, c = fixed(1, 2)
    val col = column(Size.fixed(1, 6))(a, b, c)
    val buf = render(col, Rect(0, 0, 1, 6))
    assertEquals(rectOf(buf, a.mark), Some(Rect(0, 0, 1, 2)))
    assertEquals(rectOf(buf, b.mark), Some(Rect(0, 2, 1, 2)))
    assertEquals(rectOf(buf, c.mark), Some(Rect(0, 4, 1, 2)))
    assertNoOverlap(buf, a, b, c)
    assertWithin(Rect(0, 0, 1, 6), buf, a, b, c)
  }

  test("fixed children honour a non-zero bounds origin") {
    val a, b = fixed(1, 2)
    val col = column(Size.fixed(1, 4))(a, b)
    val buf = render(col, Rect(3, 5, 1, 4))
    assertEquals(rectOf(buf, a.mark), Some(Rect(3, 5, 1, 2)))
    assertEquals(rectOf(buf, b.mark), Some(Rect(3, 7, 1, 2)))
  }

  // ---------------------------------------------------------------------------
  // Weight & percentage children — flexible stacking
  // ---------------------------------------------------------------------------

  test("equal-weight children split the column without overlap") {
    val a, b, c = weight(1)
    val col = column(Size.fixed(1, 6))(a, b, c)
    val buf = render(col, Rect(0, 0, 1, 6))
    assertEquals(rectOf(buf, a.mark), Some(Rect(0, 0, 1, 2)))
    assertEquals(rectOf(buf, b.mark), Some(Rect(0, 2, 1, 2)))
    assertEquals(rectOf(buf, c.mark), Some(Rect(0, 4, 1, 2)))
    assertNoOverlap(buf, a, b, c)
    assertWithin(Rect(0, 0, 1, 6), buf, a, b, c)
  }

  test("fixed then weight: weight takes the remaining height") {
    val a = fixed(1, 2)
    val b = weight(1)
    val col = column(Size.fixed(1, 6))(a, b)
    val buf = render(col, Rect(0, 0, 1, 6))
    assertEquals(rectOf(buf, a.mark), Some(Rect(0, 0, 1, 2)))
    assertEquals(rectOf(buf, b.mark), Some(Rect(0, 2, 1, 4)))
    assertWithin(Rect(0, 0, 1, 6), buf, a, b)
  }

  test("percentages that sum to 1.0 tile the column exactly") {
    val a = percent(0.5)
    val b = percent(0.5)
    val col = column(Size.fixed(1, 10))(a, b)
    val buf = render(col, Rect(0, 0, 1, 10))
    assertEquals(rectOf(buf, a.mark), Some(Rect(0, 0, 1, 5)))
    assertEquals(rectOf(buf, b.mark), Some(Rect(0, 5, 1, 5)))
    assertNoOverlap(buf, a, b)
    assertWithin(Rect(0, 0, 1, 10), buf, a, b)
  }

  test("percentages that sum to over 1.0 must not overflow the column") {
    // Two children each asking for 60% of a 10-row column. They cannot both be
    // satisfied; the policy scales them down proportionally to fit, so they sit
    // within the column without overlapping or spilling outside it.
    val a = percent(0.6)
    val b = percent(0.6)
    val col = column(Size.fixed(1, 10))(a, b)
    val buf = render(col, Rect(0, 0, 1, 10))
    assertNoOverlap(buf, a, b)
    assertWithin(Rect(0, 0, 1, 10), buf, a, b)
  }

  // ---------------------------------------------------------------------------
  // Over-subscribed fixed children — must not overflow
  // ---------------------------------------------------------------------------

  test("fixed children taller than the column must not overflow") {
    // Three 3-row children in a 6-row column: only the first two fit, so the
    // third is pushed past the bottom and clipped away entirely. Nothing is
    // painted outside bounds.
    val a, b, c = fixed(1, 3)
    val col = column(Size.fixed(1, 6))(a, b, c)
    val buf = render(col, Rect(0, 0, 1, 6))
    assertEquals(rectOf(buf, a.mark), Some(Rect(0, 0, 1, 3)))
    assertEquals(rectOf(buf, b.mark), Some(Rect(0, 3, 1, 3)))
    assertEquals(
      rectOf(buf, c.mark),
      None,
      "the over-subscribed child must not draw"
    )
    assertWithin(Rect(0, 0, 1, 6), buf, a, b, c)
  }

  test(
    "an over-subscribed nested column must not draw over its outer sibling"
  ) {
    // Outer column, 6 rows, holds an inner column (allocated 3 rows) above a
    // sibling that occupies rows 3..6. The inner column is over-subscribed: two
    // 3-row children in a 3-row box. If the inner column let its second child
    // overflow, that child would land on rows 3..6 — on top of the sibling.
    val innerA, innerB = fixed(1, 3)
    val inner = column(Size.fixed(1, 3))(innerA, innerB)
    val sibling = fixed(1, 3)
    val outer = column(Size.fixed(1, 6))(inner, sibling)

    val buf = render(outer, Rect(0, 0, 1, 6))

    assertEquals(rectOf(buf, sibling.mark), Some(Rect(0, 3, 1, 3)))
    // Neither inner child may paint on the sibling's rows (>= 3).
    val innerCells = cellsOf(buf, innerA.mark) ++ cellsOf(buf, innerB.mark)
    assert(
      innerCells.forall((_, y) => y < 3),
      s"inner children painted onto the sibling's rows: $innerCells"
    )
  }

  // ---------------------------------------------------------------------------
  // Justify — all six variants keep children inside the column and apart
  // ---------------------------------------------------------------------------

  for j <- List(
      Justify.Start,
      Justify.End,
      Justify.Center,
      Justify.SpaceBetween,
      Justify.SpaceAround,
      Justify.SpaceEvenly
    )
  do
    test(s"Justify.$j keeps children apart and within bounds") {
      val a, b, c = fixed(1, 1)
      val col = column(Size.fixed(1, 9), LayoutStyle(justify = j))(a, b, c)
      val buf = render(col, Rect(0, 0, 1, 9))
      assertNoOverlap(buf, a, b, c)
      assertWithin(Rect(0, 0, 1, 9), buf, a, b, c)
    }

  // ---------------------------------------------------------------------------
  // Align — cross-axis placement of a narrow child in a wide column
  // ---------------------------------------------------------------------------

  test("Align.Start places a narrow child at the left edge") {
    val a = fixed(2, 1)
    val col = column(Size.fixed(6, 1), LayoutStyle(align = Align.Start))(a)
    assertEquals(
      rectOf(render(col, Rect(0, 0, 6, 1)), a.mark),
      Some(Rect(0, 0, 2, 1))
    )
  }

  test("Align.Center centres a narrow child on the cross axis") {
    val a = fixed(2, 1)
    val col = column(Size.fixed(6, 1), LayoutStyle(align = Align.Center))(a)
    assertEquals(
      rectOf(render(col, Rect(0, 0, 6, 1)), a.mark),
      Some(Rect(2, 0, 2, 1))
    )
  }

  test("Align.End places a narrow child at the right edge") {
    val a = fixed(2, 1)
    val col = column(Size.fixed(6, 1), LayoutStyle(align = Align.End))(a)
    assertEquals(
      rectOf(render(col, Rect(0, 0, 6, 1)), a.mark),
      Some(Rect(4, 0, 2, 1))
    )
  }

  test("Align.Stretch widens a child to the column width") {
    val a = fixed(2, 1)
    val col = column(Size.fixed(6, 1), LayoutStyle(align = Align.Stretch))(a)
    assertEquals(
      rectOf(render(col, Rect(0, 0, 6, 1)), a.mark),
      Some(Rect(0, 0, 6, 1))
    )
  }

  // ---------------------------------------------------------------------------
  // measure / render agreement
  // ---------------------------------------------------------------------------

  test("a fixed-size column measures to its declared size") {
    val col = column(Size.fixed(4, 7))(fixed(1, 1))
    val dims = col.measure(Constraint.loose(Infinity, Infinity))
    assertEquals(dims, Dimensions(4, 7))
  }

  test("the height a child is measured with matches the height it renders at") {
    // A weight child should be rendered at exactly the height the column
    // reserved for it when it measured itself; a mismatch is how a child ends
    // up drawing over its neighbours.
    val a = fixed(1, 2)
    val b = weight(1)
    val col = column(Size.fixed(1, 10))(a, b)
    val measured = col.measure(Constraint.tight(1, 10))
    val buf = render(col, Rect(0, 0, measured.width, measured.height))
    assertEquals(measured.height, 10)
    assertEquals(rectOf(buf, a.mark), Some(Rect(0, 0, 1, 2)))
    assertEquals(rectOf(buf, b.mark), Some(Rect(0, 2, 1, 8)))
    assertWithin(Rect(0, 0, measured.width, measured.height), buf, a, b)
  }
