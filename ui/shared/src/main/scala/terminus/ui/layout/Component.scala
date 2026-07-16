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

import terminus.ui.capability.Observe

/** A component is something that can be rendered to the terminal and
  * participates in layout.
  *
  * The protocol for calling the methods on Component is:
  *
  *   - size (intrinsic methods may also be called here); then
  *   - measure (intrinsic methods may also be called here); then
  *   - render
  *
  * A rendering pass runs inside a reactive tracking context (the [[Observe]]
  * capability): reading a signal with `get` anywhere in measure, the
  * intrinsics, or render subscribes the enclosing render effect, so a change to
  * that signal triggers the next frame. There is no separate
  * dependency-declaration step — the read is the subscription.
  */
trait Component:
  /** The amount of space this Component wishes to occupy. */
  def size: Size

  /** Measure this component against a parent-supplied Constraint, returning the
    * size it wants to occupy. The result MUST satisfy `constraint` (i.e. equal
    * `constraint.constrain(result)`). No buffer writes and no observable
    * mutation (reactive reads register subscriptions, which is idempotent) — a
    * parent may call this more than once per layout pass.
    */
  def measure(constraint: Constraint)(using Observe): Dimensions

  /** Minimum width at which content still renders correctly, given a height.
    * `height` may be Infinity ("unbounded").
    */
  def minIntrinsicWidth(height: Int | Infinity)(using Observe): Int

  /** Width beyond which the component would not grow, given a height. */
  def maxIntrinsicWidth(height: Int | Infinity)(using Observe): Int

  /** Minimum height at which content still renders correctly, given a width.
    * `width` may be Infinity ("unbounded").
    */
  def minIntrinsicHeight(width: Int | Infinity)(using Observe): Int

  /** Height beyond which the component would not grow, given a width. */
  def maxIntrinsicHeight(width: Int | Infinity)(using Observe): Int

  /** Draw the component to the Buffer with the given Dimensions. The component
    * should always start at (0, 0).
    */
  def render(dimensions: Dimensions, buf: Buffer)(using Observe): Unit
