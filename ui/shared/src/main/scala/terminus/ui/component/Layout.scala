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

package terminus.ui.component

import terminus.ui.Constraint

/** Layout utilities. */
private object Layout:
  def resolveAxis(
      constraints: IndexedSeq[Constraint],
      available: Int
  ): IndexedSeq[Int] =
    // Phase 1: fixed cells
    val fixedTotal = constraints.collect { case Constraint.Fixed(n) => n }.sum
    val afterFixed = (available - fixedTotal).max(0)
    // Phase 2: percentages resolved against what remains after fixed
    val percentageTotal =
      constraints.collect { case Constraint.Percentage(p) =>
        (afterFixed * p).toInt
      }.sum
    val afterPercentage = (afterFixed - percentageTotal).max(0)
    // Phase 3: weights share whatever is left
    val totalWeight = constraints.collect { case Constraint.Weight(w) => w }.sum
    constraints.map:
      case Constraint.Fixed(n)      => n
      case Constraint.Percentage(p) => (afterFixed * p).toInt
      case Constraint.Weight(w)     =>
        if totalWeight > 0 then
          (afterPercentage.toDouble * w / totalWeight).toInt
        else 0

  def resolveSingle(constraint: Constraint, available: Int): Int =
    constraint match
      case Constraint.Fixed(n)      => n
      case Constraint.Percentage(p) => (available * p).toInt
      case Constraint.Weight(_)     => available
