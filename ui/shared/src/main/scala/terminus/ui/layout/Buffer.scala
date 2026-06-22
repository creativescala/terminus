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

import terminus.effect.AnsiCodes
import terminus.ui.style.CellStyle
import terminus.ui.style.Color
import terminus.ui.style.Underline
import terminus.ui.text.Line

/** Represents a writable two-dimensional array of [[Cell]]. */
trait Buffer:
  /** Write a single cell at (x, y). Out-of-bounds writes are ignored. */
  def put(x: Int, y: Int, cell: Cell): Unit

  /** Fill a rectangular region with a cell. Clips to buffer bounds. */
  def fill(rect: Rect, cell: Cell): Unit

  /** Write a [[terminus.ui.text.Line]] starting at (x, y).
    *
    * Iterates over Unicode code points (not raw Java chars) so that characters
    * outside the Basic Multilingual Plane — including most emoji — are handled
    * correctly. Wide characters (display width 2) occupy two buffer cells: the
    * left cell holds the character and the right cell holds
    * [[Cell.continuation]] as a sentinel. Zero-width code points (combining
    * marks, variation selectors, ZWJ) are skipped; full grapheme cluster
    * composition is not currently supported. Clips to buffer bounds.
    */
  def putLine(x: Int, y: Int, l: Line, style: CellStyle): Unit

  /** Construct a Buffer that writes to the same place as this Buffer, but
    * restricts writes to [[rect]]. Additionally, the returned Buffer resets the
    * coordinate system so that write to the origin write to the top-left corner
    * of [[rect]]. The returned Buffer truncates to the bounds of the underlying
    * Buffer, so writes to any parts of [[rect]] that are outside those bounds
    * will be silently ignored.
    */
  def view(rect: Rect): Buffer
