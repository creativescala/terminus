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

/** The resolved properties a [[terminus.ui.component.Button]] renders with: the
  * box (border and padding) and the label (cell) properties.
  */
final case class ButtonProps(box: BoxProps, content: CellProps):
  /** Replace the box properties with the given properties. */
  def withBox(box: BoxProps): ButtonProps =
    copy(box = box)

  /** Update the box properties using the given function. */
  def withBox(update: BoxProps => BoxProps): ButtonProps =
    copy(box = update(box))

  /** Replace the content properties with the given properties. */
  def withContent(content: CellProps): ButtonProps =
    copy(content = content)

  /** Update the content properties using the given function. */
  def withContent(update: CellProps => CellProps): ButtonProps =
    copy(content = update(content))

object ButtonProps:
  val default: ButtonProps = ButtonProps(BoxProps.default, CellProps.default)

  // Lift the ButtonProps combinators onto styles over ButtonProps, where they
  // operate on the base properties. This lets users write
  // `ButtonStyle.default.withContent(...)` without going through withBase.
  extension [State](style: Style[State, ButtonProps])
    def withBox(box: BoxProps): Style[State, ButtonProps] =
      style.withBase(_.withBox(box))

    def withBox(update: BoxProps => BoxProps): Style[State, ButtonProps] =
      style.withBase(_.withBox(update))

    def withContent(content: CellProps): Style[State, ButtonProps] =
      style.withBase(_.withContent(content))

    def withContent(update: CellProps => CellProps): Style[State, ButtonProps] =
      style.withBase(_.withContent(update))
