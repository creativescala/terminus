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

final case class LayoutProps(
    justify: Justify = Justify.Start,
    align: Align = Align.Stretch
):
  /** Replace the main-axis distribution of free space. */
  def withJustify(justify: Justify): LayoutProps =
    copy(justify = justify)

  /** Replace the cross-axis placement of children. */
  def withAlign(align: Align): LayoutProps =
    copy(align = align)

object LayoutProps:
  val default: LayoutProps = LayoutProps()
