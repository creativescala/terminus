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

/** A Buffer that wraps some underlying Buffer, restricting writes to [[bounds]]
  * and rewriting coordinates so that the origin of this Buffer maps to the
  * top-left corner of [[bounds]] in the underlying Buffer.
  *
  * Only [[put]] is defined here; [[fill]] and [[putLine]] are inherited from
  * [[Buffer]], which builds them on [[put]], so they clip and translate the
  * same way. `bounds.x`/`bounds.y` are the offset into the underlying Buffer,
  * while `bounds.width`/`bounds.height` are the size of this view's coordinate
  * space — the two must not be conflated.
  */
final class ViewBuffer(bounds: Rect, buffer: Buffer) extends Buffer:
  def put(x: Int, y: Int, cell: Cell): Unit =
    if x >= 0 && x < bounds.width && y >= 0 && y < bounds.height then
      buffer.put(x + bounds.x, y + bounds.y, cell)

  def view(rect: Rect): Buffer =
    ViewBuffer(rect, this)
