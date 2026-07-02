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

package terminus.ui.style

/** How a layout distributes children, and any leftover free space, along its
  * **main axis** — the direction it stacks children. In a
  * [[terminus.ui.component.Row]] the main axis is horizontal; in a
  * [[terminus.ui.component.Column]] it is vertical. "Free space" is the
  * container's extent on that axis minus the combined extent of its children;
  * when children already fill (or overflow) the axis these all place them from
  * the start. Mirrors CSS flexbox `justify-content`.
  */
enum Justify:
  /** Pack children against the start of the axis; all free space follows them.
    */
  case Start

  /** Pack children against the end of the axis; all free space precedes them.
    */
  case End

  /** Pack children together in the centre, with half the free space on each
    * side.
    */
  case Center

  /** Spread children across the axis with equal gaps between them and none at
    * the ends, so the first and last children touch the edges.
    */
  case SpaceBetween

  /** Give each child an equal amount of space around it. Because neighbouring
    * children each contribute their own margin, the gap between two children is
    * twice the space at the two ends.
    */
  case SpaceAround

  /** Distribute free space so that every gap is equal, including the two at the
    * ends — equal spacing before the first child, between each pair, and after
    * the last.
    */
  case SpaceEvenly
