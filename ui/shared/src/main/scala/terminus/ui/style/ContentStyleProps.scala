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

trait ContentStyleProps[A <: ContentStyleProps[A]]:
  self: WithCopy[A] =>

  protected var contentStyle: CellStyle = CellStyle.default
  def content: CellStyle = contentStyle

  /** Replace the current content style properties with the given properties. */
  def withContent(content: CellStyle): A =
    withContent(_ => content)

  /** Update the current content style properties using the given function. */
  def withContent(update: CellStyle => CellStyle): A =
    val updatedStyle = update(contentStyle)
    val updated = self.copy()
    updated.contentStyle = updatedStyle
    updated
