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

/** Base type for text style, avoids infinite recursion in focused style. */
final class BaseTextStyle()
    extends BoxStyleProps[BaseTextStyle],
      ContentStyleProps[BaseTextStyle],
      WithCopy[BaseTextStyle]:
  // IMPORTANT: copy() must mirror every mixin field. If a new mixin is added to
  // BaseTextStyle, add the corresponding assignment here or withBox/withContent
  // calls on copies will silently share state with the original.
  def copy(): BaseTextStyle =
    val copy = BaseTextStyle()
    copy.boxStyle = this.boxStyle
    copy.contentStyle = this.contentStyle
    copy

/** Styling for [[terminus.ui.component.Text]] components. */
final class TextStyle()
    extends BoxStyleProps[TextStyle],
      ContentStyleProps[TextStyle],
      FocusStyleProps[BaseTextStyle, TextStyle](BaseTextStyle()),
      WithCopy[TextStyle]:
  // IMPORTANT: copy() must mirror every mixin field. If a new mixin is added to
  // TextStyle, add the corresponding assignment here or withBox/withContent/
  // withFocus calls on copies will silently share state with the original.
  def copy(): TextStyle =
    val copy = TextStyle()
    copy.boxStyle = this.boxStyle
    copy.contentStyle = this.contentStyle
    copy.focusStyle = this.focusStyle.map(_.copy())
    copy

object TextStyle:
  val default: TextStyle = TextStyle()
