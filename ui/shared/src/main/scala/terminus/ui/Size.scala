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

/** The layout size of a component: a constraint for each axis.
  *
  * For components with a known fixed size use [[Size.fixed]]. Percentage and
  * weight constraints are resolved by the parent layout container at render
  * time.
  */
final case class Size(width: Constraint, height: Constraint):

  /** Resolve this size to concrete [[Dimensions]].
    *
    * Only valid when both constraints are [[Constraint.Fixed]]; non-fixed
    * constraints resolve to 0 until the layout algorithm is extended to handle
    * them.
    */
  def toDimensions: Dimensions =
    val w = width match
      case Constraint.Fixed(n) => n
      case _                   => 0
    val h = height match
      case Constraint.Fixed(n) => n
      case _                   => 0
    Dimensions(w, h)

object Size:
  def fixed(width: Int, height: Int): Size =
    Size(Constraint.Fixed(width), Constraint.Fixed(height))

  val zero: Size = fixed(0, 0)
