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

/** Styling attributes for a single terminal cell. */
final case class CellProps(
    fg: Color = Color.Default,
    bg: Color = Color.Default,
    bold: Boolean = false,
    italic: Boolean = false,
    underline: Underline = Underline.None,
    invert: Boolean = false,
    strikethrough: Boolean = false,
    blink: Boolean = false
):
  def withForeground(color: Color): CellProps =
    this.copy(fg = color)

  def withBackground(color: Color): CellProps =
    this.copy(bg = color)

  def withBold: CellProps =
    this.copy(bold = true)

  def withoutBold: CellProps =
    this.copy(bold = false)

  def withItalic: CellProps =
    this.copy(italic = true)

  def withoutItalic: CellProps =
    this.copy(italic = false)

  def withUnderline(underline: Underline): CellProps =
    this.copy(underline = underline)

  def withoutUnderline: CellProps =
    this.copy(underline = Underline.None)

  def withInvert: CellProps =
    this.copy(invert = true)

  def withoutInvert: CellProps =
    this.copy(invert = false)

  def withStrikethrough: CellProps =
    this.copy(strikethrough = true)

  def withoutStrikethrough: CellProps =
    this.copy(strikethrough = false)

  def withBlink: CellProps =
    this.copy(blink = true)

  def withoutBlink: CellProps =
    this.copy(blink = false)
object CellProps:
  val default: CellProps = CellProps()
