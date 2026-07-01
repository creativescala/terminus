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

package terminus.ui.layout

import munit.FunSuite
import terminus.StringBuilderTerminal
import terminus.effect.AnsiCodes
import terminus.ui.Terminal
import terminus.ui.style.CellStyle
import terminus.ui.text.Line

/** Exercises [[ViewBuffer]] through a backing [[CellArrayBuffer]]. The point of
  * a view is that writes are translated by its offset and clipped to its size;
  * these are regression tests for offset views, where the offset
  * (`bounds.x`/`bounds.y`) and the size (`bounds.width`/`bounds.height`) must
  * not be conflated. We assert by rendering the *backing* buffer, so we see
  * exactly which absolute cells a view touched — and, crucially, which it left
  * alone.
  */
class ViewBufferSuite extends FunSuite:

  val reset: String = AnsiCodes.sgr("0")

  def capture(f: Terminal ?=> Unit): String =
    val t = StringBuilderTerminal()
    f(using t)
    t.result()

  def moveTo(col: Int, row: Int): String = AnsiCodes.cursor.to(col, row)

  def renderFull(buf: CellArrayBuffer): String = capture(buf.render)

  def cell(c: Char): Cell = Cell(c.toInt, CellStyle.default)

  // ---------------------------------------------------------------------------
  // put — translation and horizontal clipping
  // ---------------------------------------------------------------------------

  test("put translates local coordinates by the view offset") {
    val backing = CellArrayBuffer(5, 1)
    val view = backing.view(Rect(1, 0, 2, 1))
    view.put(0, 0, cell('A'))
    view.put(1, 0, cell('B'))
    // Local (0,0) and (1,0) land at absolute (1,0) and (2,0).
    val out = renderFull(backing)
    assertEquals(
      out,
      reset + moveTo(1, 1) + " " + "A" + "B" + " " + " " + reset
    )
  }

  test(
    "put clips writes past the view width instead of bleeding onto a sibling"
  ) {
    val backing = CellArrayBuffer(5, 1)
    val view = backing.view(Rect(1, 0, 2, 1))
    // Local x=2 is outside a 2-wide view. The old bug compared against
    // bounds.right (= 3) and would have written to absolute (3,0).
    view.put(2, 0, cell('C'))
    val out = renderFull(backing)
    assertEquals(
      out,
      reset + moveTo(1, 1) + " " + " " + " " + " " + " " + reset
    )
  }

  test("put clips writes past the view height and negative coordinates") {
    val backing = CellArrayBuffer(2, 2)
    val view = backing.view(Rect(0, 0, 2, 1))
    view.put(0, 1, cell('A')) // y outside a 1-high view
    view.put(0, -1, cell('B'))
    view.put(-1, 0, cell('C'))
    val out = renderFull(backing)
    assertEquals(
      out,
      reset + moveTo(1, 1) + " " + " " + moveTo(1, 2) + " " + " " + reset
    )
  }

  // ---------------------------------------------------------------------------
  // fill — inherited default, translated and clipped through put
  // ---------------------------------------------------------------------------

  test("fill translates and clips an oversized rect to the view") {
    val backing = CellArrayBuffer(5, 1)
    val view = backing.view(Rect(1, 0, 2, 1))
    // Ask to fill far wider than the view; only the 2 cells at absolute
    // (1,0)-(2,0) should be touched.
    view.fill(Rect(0, 0, 5, 1), cell('X'))
    val out = renderFull(backing)
    assertEquals(
      out,
      reset + moveTo(1, 1) + " " + "X" + "X" + " " + " " + reset
    )
  }

  // ---------------------------------------------------------------------------
  // putLine — inherited default, translated and clipped through put
  // ---------------------------------------------------------------------------

  test("putLine translates and clips a line to the view width") {
    val backing = CellArrayBuffer(6, 1)
    val view = backing.view(Rect(2, 0, 2, 1))
    // "ABCD" starts at absolute (2,0); C and D fall outside the 2-wide view.
    view.putLine(0, 0, Line("ABCD"), CellStyle.default)
    val out = renderFull(backing)
    assertEquals(
      out,
      reset + moveTo(1, 1) + " " + " " + "A" + "B" + " " + " " + reset
    )
  }

  test("putLine places a wide character and its continuation within the view") {
    val backing = CellArrayBuffer(5, 1)
    val view = backing.view(Rect(1, 0, 3, 1))
    // 😀 is width 2 (absolute (1,0) + continuation at (2,0)), then X at (3,0).
    view.putLine(0, 0, Line("😀X"), CellStyle.default)
    // The continuation cell (codePoint 0) is skipped by render.
    val out = renderFull(backing)
    assertEquals(
      out,
      reset + moveTo(1, 1) + " " + "😀" + "X" + " " + reset
    )
  }

  // ---------------------------------------------------------------------------
  // nested views — clipping composes
  // ---------------------------------------------------------------------------

  test("nested views clip against every enclosing view") {
    val backing = CellArrayBuffer(6, 1)
    val outer = backing.view(Rect(1, 0, 3, 1)) // absolute x in [1, 4)
    val inner = outer.view(Rect(1, 0, 5, 1)) // offset 1 within outer, width 5
    inner.put(0, 0, cell('A')) // outer local 1 -> absolute 2
    inner.put(1, 0, cell('B')) // outer local 2 -> absolute 3
    // inner allows local x=2 (width 5), but outer local 3 is outside its
    // width 3, so this is clipped by the outer view.
    inner.put(2, 0, cell('C'))
    val out = renderFull(backing)
    assertEquals(
      out,
      reset + moveTo(1, 1) + " " + " " + "A" + "B" + " " + " " + reset
    )
  }
