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
final case class TextProps(box: BoxStyle, content: CellStyle):
  /** Replace the box style properties with the given properties. */
  def withBox(box: BoxStyle): TextProps =
    copy(box = box)

  /** Update the box style properties using the given function. */
  def withBox(update: BoxStyle => BoxStyle): TextProps =
    copy(box = update(box))

  /** Replace the content style properties with the given properties. */
  def withContent(content: CellStyle): TextProps =
    copy(content = content)

  /** Update the content style properties using the given function. */
  def withContent(update: CellStyle => CellStyle): TextProps =
    copy(content = update(content))

object TextProps:
  val default: TextProps = TextProps(BoxStyle.default, CellStyle.default)

  // Lift the TextProps combinators onto styles over TextProps, where they
  // operate on the base properties. This lets users write
  // `TextStyle.default.withContent(...)` without going through withBase.
  extension [State](style: Style[State, TextProps])
    def withBox(box: BoxStyle): Style[State, TextProps] =
      style.withBase(_.withBox(box))

    def withBox(update: BoxStyle => BoxStyle): Style[State, TextProps] =
      style.withBase(_.withBox(update))

    def withContent(content: CellStyle): Style[State, TextProps] =
      style.withBase(_.withContent(content))

    def withContent(update: CellStyle => CellStyle): Style[State, TextProps] =
      style.withBase(_.withContent(update))
