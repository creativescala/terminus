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

/** The styling properties associated with a TextInput component: the box,
  * content, and cursor properties.
  */
final case class TextInputProps(
    box: BoxProps,
    content: CellProps,
    cursor: CellProps
):
  /** Replace the box style properties with the given properties. */
  def withBox(box: BoxProps): TextInputProps =
    copy(box = box)

  /** Update the box style properties using the given function. */
  def withBox(update: BoxProps => BoxProps): TextInputProps =
    copy(box = update(box))

  /** Replace the content style properties with the given properties. */
  def withContent(content: CellProps): TextInputProps =
    copy(content = content)

  /** Update the content style properties using the given function. */
  def withContent(update: CellProps => CellProps): TextInputProps =
    copy(content = update(content))

  /** Replace the cursor style properties with the given properties. */
  def withCursor(cursor: CellProps): TextInputProps =
    copy(cursor = cursor)

  /** Update the cursor style properties using the given function. */
  def withCursor(update: CellProps => CellProps): TextInputProps =
    copy(cursor = update(cursor))

object TextInputProps:
  val default: TextInputProps =
    TextInputProps(
      BoxProps.default,
      CellProps.default,
      CellProps(invert = true)
    )

  // Lift the TextInputProps combinators onto styles over TextInputProps, where
  // they operate on the base properties. This lets users write
  // `TextInputStyle.default.withContent(...)` without going through withBase.
  extension [State](style: Style[State, TextInputProps])
    def withBox(box: BoxProps): Style[State, TextInputProps] =
      style.withBase(_.withBox(box))

    def withBox(update: BoxProps => BoxProps): Style[State, TextInputProps] =
      style.withBase(_.withBox(update))

    def withContent(content: CellProps): Style[State, TextInputProps] =
      style.withBase(_.withContent(content))

    def withContent(
        update: CellProps => CellProps
    ): Style[State, TextInputProps] =
      style.withBase(_.withContent(update))

    def withCursor(cursor: CellProps): Style[State, TextInputProps] =
      style.withBase(_.withCursor(cursor))

    def withCursor(
        update: CellProps => CellProps
    ): Style[State, TextInputProps] =
      style.withBase(_.withCursor(update))
