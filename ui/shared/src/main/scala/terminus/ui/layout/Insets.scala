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

/** The non-content space around a component's content, in cells, per side.
  *
  * This is the bridge between the box model (border, padding, one day margin)
  * and layout: it knows how to shrink the space offered to content
  * ([[deflate]]) and grow a content size back to the full outer size
  * ([[inflate]]). The two are inverses.
  */
final case class Insets(top: Int, right: Int, bottom: Int, left: Int):

  /** Total horizontal inset (left + right). */
  def horizontal: Int = left + right

  /** Total vertical inset (top + bottom). */
  def vertical: Int = top + bottom

  /** Shrink a Constraint to the space available to content. Mins floor at 0; an
    * Infinity max stays Infinity (unbounded minus a finite inset is still
    * unbounded).
    */
  def deflate(c: Constraint): Constraint =
    Constraint(
      minWidth = (c.minWidth - horizontal).max(0),
      maxWidth = Insets.subtract(c.maxWidth, horizontal),
      minHeight = (c.minHeight - vertical).max(0),
      maxHeight = Insets.subtract(c.maxHeight, vertical)
    )

  /** Grow a content size to the full outer size. Inverse of [[deflate]]. */
  def inflate(d: Dimensions): Dimensions =
    Dimensions(d.width + horizontal, d.height + vertical)

object Insets:
  val zero: Insets = Insets(0, 0, 0, 0)

  /** The same inset on every side. */
  def all(n: Int): Insets = Insets(n, n, n, n)

  /** Independent horizontal and vertical insets. */
  def symmetric(horizontal: Int, vertical: Int): Insets =
    Insets(vertical, horizontal, vertical, horizontal)

  /** Subtract a finite amount from an `Int | Infinity`, flooring at 0. Infinity
    * is absorbing: Infinity - n == Infinity.
    */
  private def subtract(value: Int | Infinity, n: Int): Int | Infinity =
    value match
      case Infinity => Infinity
      case v: Int   => (v - n).max(0)
