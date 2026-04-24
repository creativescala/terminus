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

package terminus.ui.component

import terminus.ui.Buffer
import terminus.ui.Component
import terminus.ui.LayoutContext
import terminus.ui.Rect
import terminus.ui.Size
import terminus.ui.style.ComponentStyle
import terminus.ui.style.Style
import terminus.ui.tool.Box

object Text:
  /** Create a Text component.
    *
    * When `height` is 0 (the default) the height is computed from the content:
    * the number of `\n`-separated lines plus any border/padding overhead.
    * Pass an explicit positive `height` to fix the size regardless of content.
    */
  def component(
      width: Int,
      height: Int = 0,
      text: => String,
      box: ComponentStyle = ComponentStyle.default,
      content: Style = Style.default
  ): Component =
    new Component:
      def size: Size =
        if height > 0 then Size(width, height)
        else
          val offset = (if box.border.isDefined then 1 else 0) + box.padding
          val lineCount = text.split('\n').length.max(1)
          Size(width, lineCount + 2 * offset)

      def render(bounds: Rect, buf: Buffer): Unit =
        Box.render(bounds, box, buf)
        val inner = Box.innerRect(bounds, box)
        buf.putString(inner.x, inner.y, text, content)

  def apply(
      width: Int,
      height: Int = 0,
      box: ComponentStyle = ComponentStyle.default,
      content: Style = Style.default
  )(text: => String)(using ctx: LayoutContext): Unit =
    ctx.add(component(width, height, text, box, content))
