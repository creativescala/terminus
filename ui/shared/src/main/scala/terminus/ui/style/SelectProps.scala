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

/** The styling properties associated with a Select component: the box, content,
  * and selected properties. The selected properties define the styling for the
  * selected element separately from the unselected elements.
  */
final case class SelectProps(
    box: BoxProps,
    content: CellProps,
    selected: CellProps
):
  /** Replace the box style properties with the given properties. */
  def withBox(box: BoxProps): SelectProps =
    copy(box = box)

  /** Update the box style properties using the given function. */
  def withBox(update: BoxProps => BoxProps): SelectProps =
    copy(box = update(box))

  /** Replace the content style properties with the given properties. */
  def withContent(content: CellProps): SelectProps =
    copy(content = content)

  /** Update the content style properties using the given function. */
  def withContent(update: CellProps => CellProps): SelectProps =
    copy(content = update(content))

  /** Replace the selected style properties with the given properties. */
  def withSelected(selected: CellProps): SelectProps =
    copy(selected = selected)

  /** Update the selected style properties using the given function. */
  def withSelected(update: CellProps => CellProps): SelectProps =
    copy(selected = update(selected))

object SelectProps:
  val default: SelectProps =
    SelectProps(BoxProps.default, CellProps.default, CellProps(invert = true))

  // Lift the SelectProps combinators onto styles over SelectProps, where they
  // operate on the base properties. This lets users write
  // `TextStyle.default.withContent(...)` without going through withBase.
  extension [State](style: Style[State, SelectProps])
    def withBox(box: BoxProps): Style[State, SelectProps] =
      style.withBase(_.withBox(box))

    def withBox(update: BoxProps => BoxProps): Style[State, SelectProps] =
      style.withBase(_.withBox(update))

    def withContent(content: CellProps): Style[State, SelectProps] =
      style.withBase(_.withContent(content))

    def withContent(update: CellProps => CellProps): Style[State, SelectProps] =
      style.withBase(_.withContent(update))

    def withSelected(selected: CellProps): Style[State, SelectProps] =
      style.withBase(_.withSelected(selected))

    def withSelected(
        update: CellProps => CellProps
    ): Style[State, SelectProps] =
      style.withBase(_.withSelected(update))
