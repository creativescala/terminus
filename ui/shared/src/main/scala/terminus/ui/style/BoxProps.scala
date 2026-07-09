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

import terminus.ui.layout.Insets

/** The properties that can be set for styling a components box: an optional
  * border, a border style, and padding.
  *
  * @param border
  *   The border to draw around the component. [[None]] means no border. Use
  *   [[Border.empty]] if you want a border that takes up space but doesn't
  *   render any characters.
  * @param borderProps
  *   Cell-level style applied to border characters.
  * @param background
  *   Cell-level style used to fill the interior (empty cells get this style).
  * @param padding
  *   Number of cells between the border and the content on each side.
  */
final case class BoxProps(
    border: Option[Border] = Some(Border.single),
    borderProps: CellProps = CellProps.default,
    padding: Int = 0,
    background: CellProps = CellProps.default
):
  def withBorder(border: Border): BoxProps =
    this.copy(border = Some(border))

  def withoutBorder: BoxProps =
    this.copy(border = None)

  def withBorderProps(style: CellProps): BoxProps =
    this.copy(borderProps = style)

  def withBorderProps(update: CellProps => CellProps): BoxProps =
    this.copy(borderProps = update(borderProps))

  def withPadding(padding: Int): BoxProps =
    this.copy(padding = padding)

  def withBackground(style: CellProps): BoxProps =
    this.copy(background = style)

  /** The cells this box occupies around its content: one per side for the
    * border (if present), plus [[padding]] on every side.
    */
  def insets: Insets =
    val borderInset = if border.isDefined then 1 else 0
    Insets.all(borderInset + padding)

object BoxProps:
  val default = BoxProps()
