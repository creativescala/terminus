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

/** The size constraint for one axis of a component. */
enum Constraint:
  /** Exactly `cells` terminal cells. */
  case Fixed(cells: Int)

  /** A fraction of the container's size on this axis (0.0–1.0). */
  case Percentage(percent: Double)

  /** A proportional share of the remaining space after fixed and percentage
    * children have been placed.
    */
  case Weight(weight: Int)
