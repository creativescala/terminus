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
import terminus.ui.style.Justify
import terminus.ui.style.LayoutStyle

import scala.collection.mutable

/** Tests focused on [[Column]] layout. Rather than inspecting rendered terminal
  * output, these record the [[Rect]] each child is asked to render into. That
  * makes the two failure modes the user reported directly observable:
  *
  *   - **overlap** — two children sharing rows, and
  *   - **overflow** — a child placed (partly) outside the column's bounds.
  */
class ColumnSuite extends FunSuite:

  /** A leaf component that records every bounds it is rendered into. Its
    * natural size is the fixed cells on each axis (0 on a flexible axis).
    */
  final class Recorder(val size: Size) extends Component:
    val rects: mutable.ArrayBuffer[Rect] = mutable.ArrayBuffer.empty

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
    def render(bounds: Rect, buf: Buffer): Unit = rects += bounds

  def recorder(size: Size): Recorder = new Recorder(size)
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

  /** Render `col` into a buffer of `bounds` and return the rect each
    * `recorders` child was rendered into (in order).
    */
  def renderRects(col: Column, bounds: Rect, recorders: Recorder*): Seq[Rect] =
    val buf = CellArrayBuffer(bounds.right.max(0), bounds.bottom.max(0))
    col.render(bounds, buf)
    recorders.map { r =>
      assert(
        r.rects.sizeIs == 1,
        s"expected child to render exactly once, got ${r.rects.toList}"
      )
      r.rects.head
    }

  /** Assert no two rects share a row (i.e. their vertical extents are
    * disjoint), in render order.
    */
  def assertNoVerticalOverlap(rects: Seq[Rect]): Unit =
    rects.sliding(2).foreach {
      case Seq(a, b) =>
        assert(
          b.y >= a.bottom,
          s"vertical overlap: $b starts above the bottom of $a (${a.bottom})"
        )
      case _ => ()
    }

  /** Assert every rect lies within `bounds`. */
  def assertWithin(bounds: Rect, rects: Seq[Rect]): Unit =
    rects.foreach { r =>
      assert(
        r.y >= bounds.y && r.bottom <= bounds.bottom,
        s"$r overflows the vertical bounds of $bounds"
      )
      assert(
        r.x >= bounds.x && r.right <= bounds.right,
        s"$r overflows the horizontal bounds of $bounds"
      )
    }

  // ---------------------------------------------------------------------------
  // Fixed children — exact stacking
  // ---------------------------------------------------------------------------

  test("fixed children stack top to bottom without overlap") {
    val a, b, c = fixed(1, 2)
    val col = column(Size.fixed(1, 6))(a, b, c)
    val rects = renderRects(col, Rect(0, 0, 1, 6), a, b, c)
    assertEquals(rects.map(_.y), List(0, 2, 4))
    assertNoVerticalOverlap(rects)
    assertWithin(Rect(0, 0, 1, 6), rects)
  }

  test("fixed children honour a non-zero bounds origin") {
    val a, b = fixed(1, 2)
    val col = column(Size.fixed(1, 4))(a, b)
    val rects = renderRects(col, Rect(3, 5, 1, 4), a, b)
    assertEquals(rects, List(Rect(3, 5, 1, 2), Rect(3, 7, 1, 2)))
  }

  // ---------------------------------------------------------------------------
  // Weight & percentage children — flexible stacking
  // ---------------------------------------------------------------------------

  test("equal-weight children split the column without overlap") {
    val a, b, c = weight(1)
    val col = column(Size.fixed(1, 6))(a, b, c)
    val rects = renderRects(col, Rect(0, 0, 1, 6), a, b, c)
    assertEquals(rects.map(r => (r.y, r.height)), List((0, 2), (2, 2), (4, 2)))
    assertNoVerticalOverlap(rects)
    assertWithin(Rect(0, 0, 1, 6), rects)
  }

  test("fixed then weight: weight takes the remaining height") {
    val a = fixed(1, 2)
    val b = weight(1)
    val col = column(Size.fixed(1, 6))(a, b)
    val rects = renderRects(col, Rect(0, 0, 1, 6), a, b)
    assertEquals(rects, List(Rect(0, 0, 1, 2), Rect(0, 2, 1, 4)))
    assertWithin(Rect(0, 0, 1, 6), rects)
  }

  test("percentages that sum to 1.0 tile the column exactly") {
    val a = percent(0.5)
    val b = percent(0.5)
    val col = column(Size.fixed(1, 10))(a, b)
    val rects = renderRects(col, Rect(0, 0, 1, 10), a, b)
    assertEquals(rects, List(Rect(0, 0, 1, 5), Rect(0, 5, 1, 5)))
    assertNoVerticalOverlap(rects)
    assertWithin(Rect(0, 0, 1, 10), rects)
  }

  test("percentages that sum to over 1.0 must not overflow the column") {
    // Two children each asking for 60% of a 10-row column. They cannot both be
    // satisfied; whatever the policy, neither should be drawn outside bounds.
    val a = percent(0.6)
    val b = percent(0.6)
    val col = column(Size.fixed(1, 10))(a, b)
    val rects = renderRects(col, Rect(0, 0, 1, 10), a, b)
    assertNoVerticalOverlap(rects)
    assertWithin(Rect(0, 0, 1, 10), rects)
  }

  // ---------------------------------------------------------------------------
  // Over-subscribed fixed children — must not overflow
  // ---------------------------------------------------------------------------

  test("fixed children taller than the column must not overflow") {
    // Three 3-row children in a 6-row column: only the first two fit, so the
    // third gets no room and is not drawn. Nothing is drawn outside bounds.
    val a, b, c = fixed(1, 3)
    val col = column(Size.fixed(1, 6))(a, b, c)
    val buf = CellArrayBuffer(1, 6)
    col.render(Rect(0, 0, 1, 6), buf)
    val drawn = Seq(a, b, c).flatMap(_.rects.toList)
    assertNoVerticalOverlap(drawn)
    assertWithin(Rect(0, 0, 1, 6), drawn)
    assertEquals(c.rects.toList, Nil, "the over-subscribed child must not draw")
  }

  test(
    "an over-subscribed nested column must not draw over its outer sibling"
  ) {
    // Outer column, 6 rows, holds an inner column (allocated 3 rows) above a
    // sibling that occupies rows 3..6. The inner column is over-subscribed: two
    // 3-row children in a 3-row box. If the inner column lets its second child
    // overflow, that child lands on rows 3..6 — on top of the sibling.
    val innerA, innerB = fixed(1, 3)
    val inner = column(Size.fixed(1, 3))(innerA, innerB)
    val sibling = fixed(1, 3)
    val outer = column(Size.fixed(1, 6))(inner, sibling)

    val buf = CellArrayBuffer(1, 6)
    outer.render(Rect(0, 0, 1, 6), buf)

    val siblingRect = sibling.rects.head
    innerA.rects.concat(innerB.rects).foreach { r =>
      assert(
        r.bottom <= siblingRect.y || r.y >= siblingRect.bottom,
        s"inner child $r overlaps the outer sibling $siblingRect"
      )
    }
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
      val rects = renderRects(col, Rect(0, 0, 1, 9), a, b, c)
      assertNoVerticalOverlap(rects)
      assertWithin(Rect(0, 0, 1, 9), rects)
    }

  // ---------------------------------------------------------------------------
  // Align — cross-axis placement of a narrow child in a wide column
  // ---------------------------------------------------------------------------

  test("Align.Start places a narrow child at the left edge") {
    val a = fixed(2, 1)
    val col = column(Size.fixed(6, 1), LayoutStyle(align = Align.Start))(a)
    assertEquals(renderRects(col, Rect(0, 0, 6, 1), a), List(Rect(0, 0, 2, 1)))
  }

  test("Align.Center centres a narrow child on the cross axis") {
    val a = fixed(2, 1)
    val col = column(Size.fixed(6, 1), LayoutStyle(align = Align.Center))(a)
    assertEquals(renderRects(col, Rect(0, 0, 6, 1), a), List(Rect(2, 0, 2, 1)))
  }

  test("Align.End places a narrow child at the right edge") {
    val a = fixed(2, 1)
    val col = column(Size.fixed(6, 1), LayoutStyle(align = Align.End))(a)
    assertEquals(renderRects(col, Rect(0, 0, 6, 1), a), List(Rect(4, 0, 2, 1)))
  }

  test("Align.Stretch widens a child to the column width") {
    val a = fixed(2, 1)
    val col = column(Size.fixed(6, 1), LayoutStyle(align = Align.Stretch))(a)
    assertEquals(renderRects(col, Rect(0, 0, 6, 1), a), List(Rect(0, 0, 6, 1)))
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
    val rects =
      renderRects(col, Rect(0, 0, measured.width, measured.height), a, b)
    assertEquals(measured.height, 10)
    assertEquals(rects.map(_.height).sum, 10)
    assertWithin(Rect(0, 0, measured.width, measured.height), rects)
  }
