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

/** How a layout positions each child on its **cross axis** — the axis
  * perpendicular to the direction it stacks children. In a [[terminus.ui.component.Row]]
  * the cross axis is vertical (height); in a [[terminus.ui.component.Column]] it
  * is horizontal (width). Each child is placed independently, so unlike
  * [[Justify]] this never depends on the other children or on leftover space.
  */
enum Align:
  /** Place the child at the start edge of the cross axis: the top of a row, the
    * left of a column.
    */
  case Start

  /** Place the child at the far edge of the cross axis: the bottom of a row, the
    * right of a column.
    */
  case End

  /** Centre the child within the cross axis, with any slack split evenly on
    * either side.
    */
  case Center

  /** Grow the child to fill the whole cross axis, ignoring its natural size on
    * that axis. The child is measured with a tight cross-axis constraint.
    */
  case Stretch
