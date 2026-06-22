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

/** Represents an unbounded amount of space in a Constraint. */
sealed trait Infinity
object Infinity extends Infinity

/** A range of acceptable sizes a parent offers a child during measurement.
  *
  * `min == max` on an axis is a *tight* constraint ("you must be exactly
  * this"); `min == 0` is *loose* ("take what you need, up to max"). A `max` of
  * [[Infinity]] means unbounded — the child may be as large as it likes.
  */
final case class Constraint(
    minWidth: Int,
    maxWidth: Int | Infinity,
    minHeight: Int,
    maxHeight: Int | Infinity
):

  /** Clamp a desired size into this range. A child should always return a
    * Dimensions that satisfies its incoming Constraint; this guarantees it.
    */
  def constrain(d: Dimensions): Dimensions =
    Dimensions(
      Constraint.clamp(d.width, minWidth, maxWidth),
      Constraint.clamp(d.height, minHeight, maxHeight)
    )

  /** Same range, but with both minimums dropped to 0. Used by wrap-content
    * parents that want each child at its natural size.
    */
  def loosen: Constraint =
    copy(minWidth = 0, minHeight = 0)

  /** A tight constraint at the given size, clamped into this range. Used to
    * force a child to fill an allocated slice (e.g. a Weight child, or a
    * Stretch cross-axis).
    */
  def tighten(width: Int, height: Int): Constraint =
    val w = Constraint.clamp(width, minWidth, maxWidth)
    val h = Constraint.clamp(height, minHeight, maxHeight)
    Constraint(w, w, h, h)

object Constraint:
  /** Clamp `value` to at least `min` and at most `max`. An [[Infinity]] max
    * imposes no upper bound.
    */
  private def clamp(value: Int, min: Int, max: Int | Infinity): Int =
    val lowerBounded = value.max(min)
    max match
      case Infinity => lowerBounded
      case m: Int   => lowerBounded.min(m)

  /** Loose on both axes, bounded by the given dimensions. */
  def loose(width: Int | Infinity, height: Int | Infinity): Constraint =
    Constraint(
      minWidth = 0,
      maxWidth = width,
      minHeight = 0,
      maxHeight = height
    )

  /** Tight on both axes. */
  def tight(width: Int, height: Int): Constraint =
    Constraint(
      minWidth = width,
      maxWidth = width,
      minHeight = height,
      maxHeight = height
    )
