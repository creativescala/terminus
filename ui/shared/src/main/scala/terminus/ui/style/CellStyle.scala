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
final case class CellStyle(
    fg: Color = Color.Default,
    bg: Color = Color.Default,
    bold: Boolean = false,
    italic: Boolean = false,
    underline: Underline = Underline.None,
    invert: Boolean = false,
    strikethrough: Boolean = false,
    blink: Boolean = false
):
  def withBold: CellStyle =
    this.copy(bold = true)

  def withoutBold: CellStyle =
    this.copy(bold = false)

  def withItalic: CellStyle =
    this.copy(italic = true)

  def withoutItalic: CellStyle =
    this.copy(italic = false)

  def withUnderline(underline: Underline): CellStyle =
    this.copy(underline = underline)

  def withoutUnderline: CellStyle =
    this.copy(underline = Underline.None)

  def withInvert: CellStyle =
    this.copy(invert = true)

  def withoutInvert: CellStyle =
    this.copy(invert = false)

  def withStrikethrough: CellStyle =
    this.copy(strikethrough = true)

  def withoutStrikethrough: CellStyle =
    this.copy(strikethrough = false)

  def withBlink: CellStyle =
    this.copy(blink = true)

  def withoutBlink: CellStyle =
    this.copy(blink = false)
object CellStyle:
  val default: CellStyle = CellStyle()
