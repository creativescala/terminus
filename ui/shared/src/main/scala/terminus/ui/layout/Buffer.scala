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

import terminus.ui.style.CellStyle
import terminus.ui.text.Line

/** Represents a writable two-dimensional array of [[Cell]]. */
trait Buffer:
  /** Write a single cell at (x, y). Out-of-bounds writes are ignored.
    *
    * This is the primitive from which [[fill]] and [[putLine]] are built: any
    * clipping and coordinate translation a Buffer performs (see [[view]]) lives
    * here, and the higher-level operations inherit it automatically.
    */
  def put(x: Int, y: Int, cell: Cell): Unit

  /** Fill a rectangular region with a cell. Clips to buffer bounds.
    *
    * Defined in terms of [[put]] so it inherits the same clipping and
    * translation. Implementations may override for efficiency but must preserve
    * these semantics.
    */
  def fill(rect: Rect, cell: Cell): Unit =
    var y = rect.y.max(0)
    val yEnd = rect.bottom
    val xStart = rect.x.max(0)
    val xEnd = rect.right
    while y < yEnd do
      var x = xStart
      while x < xEnd do
        put(x, y, cell)
        x += 1
      y += 1

  /** Write a [[terminus.ui.text.Line]] starting at (x, y).
    *
    * Iterates over Unicode code points (not raw Java chars) so that characters
    * outside the Basic Multilingual Plane — including most emoji — are handled
    * correctly. Wide characters (display width 2) occupy two buffer cells: the
    * left cell holds the character and the right cell holds
    * [[Cell.continuation]] as a sentinel. Zero-width code points (combining
    * marks, variation selectors, ZWJ) are skipped; full grapheme cluster
    * composition is not currently supported.
    *
    * Defined in terms of [[put]] so it inherits the same clipping and
    * translation. Implementations may override for efficiency but must preserve
    * these semantics.
    */
  def putLine(x: Int, y: Int, l: Line, style: CellStyle): Unit =
    val s = l.value
    var col = x
    var i = 0
    val stop = s.length
    while i < stop do
      val cp = Character.codePointAt(s, i)
      i += Character.charCount(cp)
      CharWidth.of(cp) match
        case 0 => () // zero-width: skip
        case 1 =>
          put(col, y, Cell(cp, style))
          col += 1
        case _ => // 2
          put(col, y, Cell(cp, style))
          put(col + 1, y, Cell.continuation)
          col += 2

  /** Construct a Buffer that writes through to this Buffer, but restricts
    * writes to [[rect]] and resets the coordinate system so that a write to the
    * origin lands at the top-left corner of [[rect]].
    *
    * Writes outside `rect` (local coordinates outside `[0, rect.width) × [0,
    * rect.height)`) are ignored, so a component drawn through the view cannot
    * spill onto whatever sits beside it. Views compose: taking a view of a view
    * clips against both rectangles, and writes are ultimately truncated to the
    * bounds of the underlying Buffer.
    */
  def view(rect: Rect): Buffer
