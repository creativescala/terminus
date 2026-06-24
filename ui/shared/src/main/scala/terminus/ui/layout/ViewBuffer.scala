package terminus.ui.layout

import terminus.ui.style.CellStyle
import terminus.ui.text.Line

/** A Buffer that wraps some underlying Buffer, and rewrites coordinate to match
  * the transformation provided by [[bounds]].
  */
final class ViewBuffer(bounds: Rect, buffer: Buffer) extends Buffer:
  def put(x: Int, y: Int, cell: Cell): Unit =
    if x >= 0 && x < bounds.right && y >= 0 && y < bounds.bottom then
      buffer.put(x + bounds.x, y + bounds.y, cell)
    else ()

  def fill(rect: Rect, cell: Cell): Unit =
    val x0 = rect.x.max(0)
    val y0 = rect.y.max(0)
    val x1 = rect.right.min(bounds.width)
    val y1 = rect.bottom.min(bounds.height)
    val r = Rect(x0, y0, x1 - x0, y1 - y0)
    buffer.fill(r, cell)

  def putLine(x: Int, y: Int, l: Line, style: CellStyle): Unit =
    buffer.putLine(x + bounds.x, y + bounds.y, l, style)

  def view(rect: Rect): Buffer =
    ViewBuffer(rect, this)
