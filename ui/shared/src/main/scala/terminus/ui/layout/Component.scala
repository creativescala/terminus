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

import terminus.ui.capability.React

/** A component is something that can be rendered to the terminal and
  * participates in layout.
  *
  * The protocol for calling the methods on Component is:
  *
  *   - Methods are only called once per rendering loop, except size and the
  *     intrinsic methods which may be called multiple times.
  *   - Methods are called in the order:
  *     - react, which indicates the start of a rendering loop
  *     - size (intrinsic methods may also be called here)
  *     - measure (intrinsic methods may also be called here)
  *     - render
  */
trait Component:
  /** Called at the started of a rendering pass, this is the signal to a
    * component to re-evaluate any reactives it depends on, and perform any
    * recomputations needed before layout.
    */
  def react(using React): Unit

  /** The amount of space this Component wishes to occupy. */
  def size: Size

  /** Measure this component against a parent-supplied Constraint, returning the
    * size it wants to occupy. The result MUST satisfy `constraint` (i.e. equal
    * `constraint.constrain(result)`). Pure: no buffer writes, no mutation — a
    * parent may call this more than once per layout pass.
    */
  def measure(constraint: Constraint): Dimensions

  /** Minimum width at which content still renders correctly, given a height.
    * `height` may be Infinity ("unbounded").
    */
  def minIntrinsicWidth(height: Int | Infinity): Int

  /** Width beyond which the component would not grow, given a height. */
  def maxIntrinsicWidth(height: Int | Infinity): Int

  /** Minimum height at which content still renders correctly, given a width.
    * `width` may be Infinity ("unbounded").
    */
  def minIntrinsicHeight(width: Int | Infinity): Int

  /** Height beyond which the component would not grow, given a width. */
  def maxIntrinsicHeight(width: Int | Infinity): Int

  /** Draw the component to the Buffer with the given Dimensions. The component
    * should always start at (0, 0).
    */
  def render(dimensions: Dimensions, buf: Buffer): Unit
