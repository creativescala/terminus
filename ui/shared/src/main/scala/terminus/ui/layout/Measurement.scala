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

/** A Measurement expresses a dimension of size in terms of a fixed number of
  * cells, a portion of the parent's space, or in terms of the space occupied by
  * children.
  */
enum Measurement:
  /** Exactly `cells` */
  case Fixed(cells: Int)

  /** A fraction (0.0–1.0) of the *remaining* space on this axis — what is left
    * after fixed and intrinsically-sized children have been placed — not a
    * fraction of the parent's total size. So `Fixed(10)` beside two
    * `Percentage(0.5)` children yields the fixed 10 cells and an even split of
    * everything else, and this is why `Fixed(10) + Percentage(0.5) +
    * Percentage(0.5)` fits rather than overflowing (contrast CSS, where `width:
    * 50%` is a fraction of the container and the two would overflow).
    *
    * Unlike [[Weight]], a percentage is an absolute share of that remaining
    * pool: it can under-fill, leaving a genuine gap. A lone `Percentage(0.3)`
    * occupies 30% and leaves 70% empty, whereas a lone [[Weight]] always fills
    * the pool. Percentages that together oversubscribe the pool (sum > 100%)
    * are scaled down proportionally to fit, so no child is clipped.
    */
  case Percentage(percent: Double)

  /** A proportional share of the parent's remaining space after fixed and
    * percentage children have been placed.
    */
  case Weight(weight: Int)

  /** Exactly match the aggregate size of children elements. */
  case WrapContent

  /** Match childrens' natural maximum size limits. */
  case MaxIntrinsic

  /** Match childrens' natural minimum size limits. */
  case MinIntrinsic
