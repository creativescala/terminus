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

/** The resolved properties a text-like component renders with: the box (border
  * and padding) and the content (cell) style.
  */
final case class TextProps(box: BoxProps, content: CellProps):
  /** Replace the box style properties with the given properties. */
  def withBox(box: BoxProps): TextProps =
    copy(box = box)

  /** Update the box style properties using the given function. */
  def withBox(update: BoxProps => BoxProps): TextProps =
    copy(box = update(box))

  /** Replace the content style properties with the given properties. */
  def withContent(content: CellProps): TextProps =
    copy(content = content)

  /** Update the content style properties using the given function. */
  def withContent(update: CellProps => CellProps): TextProps =
    copy(content = update(content))

object TextProps:
  val default: TextProps = TextProps(BoxProps.default, CellProps.default)

  // Lift the TextProps combinators onto styles over TextProps, where they
  // operate on the base properties. This lets users write
  // `TextStyle.default.withContent(...)` without going through withBase.
  extension [State](style: Style[State, TextProps])
    def withBox(box: BoxProps): Style[State, TextProps] =
      style.withBase(_.withBox(box))

    def withBox(update: BoxProps => BoxProps): Style[State, TextProps] =
      style.withBase(_.withBox(update))

    def withContent(content: CellProps): Style[State, TextProps] =
      style.withBase(_.withContent(content))

    def withContent(update: CellProps => CellProps): Style[State, TextProps] =
      style.withBase(_.withContent(update))
