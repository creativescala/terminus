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

/** Component-level styling: border, background fill, and padding.
  *
  * This is distinct from the cell-level [[Style]], which controls text
  * attributes (colour, bold, etc.). [[ComponentStyle]] controls the structural
  * appearance of a component's bounding box.
  *
  * @param border
  *   The border to draw around the component. [[None]] means no border.
  * @param borderStyle
  *   Cell-level style applied to border characters.
  * @param background
  *   Cell-level style used to fill the interior (empty cells get this style).
  * @param padding
  *   Number of cells between the border and the content on each side.
  */
final case class ComponentStyle(
    border: Option[Border] = Some(Border.single),
    borderStyle: Style = Style.default,
    background: Style = Style.default,
    padding: Int = 0,
    focused: Option[ComponentStyle] = None
)
object ComponentStyle:
  val default: ComponentStyle = ComponentStyle()
  val none: ComponentStyle = ComponentStyle(border = None)
