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

package terminus.ui.tool

import terminus.ui.Buffer
import terminus.ui.Cell
import terminus.ui.Rect
import terminus.ui.style.Border
import terminus.ui.style.Style

object Box:

  /** Draw a bordered box into the buffer at the given bounds. The border
    * occupies the outermost cells, so the minimum usable width and height is 2.
    * Out-of-bounds writes are silently clipped by the buffer.
    */
  def render(bounds: Rect, border: Border, style: Style, buf: Buffer): Unit =
    val x0 = bounds.x
    val y0 = bounds.y
    val x1 = bounds.x + bounds.width - 1 // inclusive right edge
    val y1 = bounds.y + bounds.height - 1 // inclusive bottom edge

    // Top row
    buf.put(x0, y0, Cell(border.topLeft, style))
    var x = x0 + 1
    while x < x1 do
      buf.put(x, y0, Cell(border.horizontal, style))
      x += 1
    buf.put(x1, y0, Cell(border.topRight, style))

    // Sides
    var y = y0 + 1
    while y < y1 do
      buf.put(x0, y, Cell(border.vertical, style))
      buf.put(x1, y, Cell(border.vertical, style))
      y += 1

    // Bottom row
    buf.put(x0, y1, Cell(border.bottomLeft, style))
    x = x0 + 1
    while x < x1 do
      buf.put(x, y1, Cell(border.horizontal, style))
      x += 1
    buf.put(x1, y1, Cell(border.bottomRight, style))
