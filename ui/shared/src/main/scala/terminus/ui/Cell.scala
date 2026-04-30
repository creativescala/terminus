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

import terminus.ui.style.CellStyle

/** A single terminal cell: a Unicode code point with associated styling.
  *
  * The code point is stored as an [[Int]] to accommodate the full Unicode
  * range, including characters outside the Basic Multilingual Plane (code
  * points > U+FFFF) such as emoji. During rendering, [[Character.toChars]] is
  * used to convert back to the one or two [[Char]] values needed by the
  * terminal.
  *
  * Use [[Cell.empty]] for an unstyled space and [[Cell.continuation]] to mark
  * the right half of a wide (2-cell) character.
  */
final case class Cell(codePoint: Int, style: CellStyle)
object Cell:
  /** An unstyled space cell — the default state of every buffer position. */
  val empty: Cell = Cell(' '.toInt, CellStyle.default)

  /** Sentinel marking the right half of a wide (2-cell) character. The flush
    * loop skips these cells; the preceding wide character already occupies both
    * columns.
    */
  val continuation: Cell = Cell(0, CellStyle.default)
