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
import terminus.ui.Rect
import terminus.ui.RenderContext
import terminus.ui.Size
import terminus.ui.style.Border
import terminus.ui.style.Style
import terminus.ui.tool.Box

object Text:
  def component(
      width: Int,
      height: Int,
      content: String,
      style: Style = Style.default
  ): Component =
    new Component:
      val size: Size = Size(width, height)

      def render(bounds: Rect, buf: Buffer): Unit =
        Box.render(bounds, Border.single, style, buf)
        buf.putString(bounds.x + 1, bounds.y + 1, content, style)

  def apply(width: Int, height: Int)(
      content: String
  )(using ctx: RenderContext): Unit =
    ctx.add(component(width, height, content))
