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

/** Mix-in for styling the box properties of components: border and padding.
  *
  * The type `A` is the type that `BoxStyle` is mixed into.
  */
trait BoxStyleProps[A <: BoxStyleProps[A]]:
  self: WithCopy[A] =>

  protected var boxStyle: BoxStyle = BoxStyle.default
  def box: BoxStyle = boxStyle

  /** Replace the current box style properties with the given properties. */
  def withBox(box: BoxStyle): A =
    withBox(_ => box)

  /** Update the current box style properties using the given function. */
  def withBox(update: BoxStyle => BoxStyle): A =
    val updatedStyle = update(boxStyle)
    val updated = self.copy()
    updated.boxStyle = updatedStyle
    updated
