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
import terminus.ui.style.ComponentStyle

object Box:

  /** The content rect inside a box: bounds shrunk by one cell for the border
    * (if present) and then by [[ComponentStyle.padding]] on each side.
    */
  def innerRect(bounds: Rect, style: ComponentStyle): Rect =
    val offset = (if style.border.isDefined then 1 else 0) + style.padding
    Rect(
      bounds.x + offset,
      bounds.y + offset,
      bounds.width - 2 * offset,
      bounds.height - 2 * offset
    )

  /** Draw a box into the buffer at the given bounds.
    *
    * Fills the interior with the background style, then draws the border (if
    * present) using the border style. The minimum usable size when a border is
    * present is 2×2. Out-of-bounds writes are silently clipped by the buffer.
    */
  def render(bounds: Rect, style: ComponentStyle, buf: Buffer): Unit =
    val borderOffset = if style.border.isDefined then 1 else 0

    // Fill interior with background style
    val fillRect = Rect(
      bounds.x + borderOffset,
      bounds.y + borderOffset,
      bounds.width - 2 * borderOffset,
      bounds.height - 2 * borderOffset
    )
    if fillRect.width > 0 && fillRect.height > 0 then
      buf.fill(fillRect, Cell(' '.toInt, style.background))

    // Draw border if present
    style.border.foreach { border =>
      val x0 = bounds.x
      val y0 = bounds.y
      val x1 = bounds.x + bounds.width - 1
      val y1 = bounds.y + bounds.height - 1
      val bs = style.borderStyle

      // Top row
      buf.put(x0, y0, Cell(border.topLeft.toInt, bs))
      var x = x0 + 1
      while x < x1 do
        buf.put(x, y0, Cell(border.horizontal.toInt, bs))
        x += 1
      buf.put(x1, y0, Cell(border.topRight.toInt, bs))

      // Sides
      var y = y0 + 1
      while y < y1 do
        buf.put(x0, y, Cell(border.vertical.toInt, bs))
        buf.put(x1, y, Cell(border.vertical.toInt, bs))
        y += 1

      // Bottom row
      buf.put(x0, y1, Cell(border.bottomLeft.toInt, bs))
      x = x0 + 1
      while x < x1 do
        buf.put(x, y1, Cell(border.horizontal.toInt, bs))
        x += 1
      buf.put(x1, y1, Cell(border.bottomRight.toInt, bs))
    }
