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

import terminus.Key
import terminus.ui.AppContext
import terminus.ui.Buffer
import terminus.ui.Component
import terminus.ui.Rect
import terminus.ui.RenderContext
import terminus.ui.Signal
import terminus.ui.Size
import terminus.ui.style.TextStyle
import terminus.ui.tool.Box

object Select:

  /** Add a scrollable selection list component.
    *
    * The component must be placed inside a [[terminus.ui.FocusScope]] to
    * receive keyboard input. The caller owns `selected` and can read it to
    * react to the user's choice:
    *
    * {{{
    * FocusScope { _ ?=>
    *   val choice = ctx.createSignal(0)
    *   Select(30, 8, items, choice)
    *   Text(30)(_.withBox(_.withoutBorder)) { s"Picked: ${items(choice.get)}" }
    * }
    * }}}
    *
    * Key bindings: Up/Down move one item, PageUp/PageDown move one page,
    * Home/End jump to first/last item. The viewport scrolls to keep the
    * selected item visible. The selected item is highlighted by inverting the
    * content style; all other items use the content style unchanged.
    *
    * @param label
    *   Converts each item to a display string. Defaults to [[Any.toString]].
    */
  def apply[A](
      width: Int,
      height: Int,
      items: Seq[A],
      selected: Signal[Int],
      style: TextStyle = TextStyle.default,
      label: A => String = (a: A) => a.toString
  )(using ctx: AppContext): Unit =
    // Compute visible row count from the unfocused box style (border/padding
    // are typically identical between focused and unfocused states).
    val ab = style.box
    val borderOffset = if ab.border.isDefined then 1 else 0
    val visibleRows = (height - 2 * (borderOffset + ab.padding)).max(0)

    val scroll = ctx.createSignal(0)

    def clampSelected(n: Int): Int = n.max(0).min((items.length - 1).max(0))

    def scrollToShow(sel: Int): Unit =
      val s = scroll.peek
      if sel < s then scroll.set(sel)
      else if sel >= s + visibleRows then scroll.set(sel - visibleRows + 1)

    ctx.onKey(Key.up) {
      val next = clampSelected(selected.peek - 1)
      selected.set(next)
      scrollToShow(next)
    }
    ctx.onKey(Key.down) {
      val next = clampSelected(selected.peek + 1)
      selected.set(next)
      scrollToShow(next)
    }
    ctx.onKey(Key.pageUp) {
      val next = clampSelected(selected.peek - visibleRows)
      selected.set(next)
      scrollToShow(next)
    }
    ctx.onKey(Key.pageDown) {
      val next = clampSelected(selected.peek + visibleRows)
      selected.set(next)
      scrollToShow(next)
    }
    ctx.onKey(Key.home) {
      selected.set(0)
      scroll.set(0)
    }
    ctx.onKey(Key.`end`) {
      val last = clampSelected(Int.MaxValue)
      selected.set(last)
      scrollToShow(last)
    }

    ctx.add(
      component(width, height, items, selected, scroll, style, label)(using ctx)
    )

  private def component[A](
      width: Int,
      height: Int,
      items: Seq[A],
      selected: Signal[Int],
      scroll: Signal[Int],
      style: TextStyle,
      label: A => String
  )(using rc: RenderContext): Component =
    new Component:
      private def activeBox =
        if rc.isFocused then style.focus.map(_.box).getOrElse(style.box)
        else style.box
      private def activeContent =
        if rc.isFocused then style.focus.map(_.content).getOrElse(style.content)
        else style.content

      def size: Size = Size.fixed(width, height)

      def render(bounds: Rect, buf: Buffer): Unit =
        val ab = activeBox
        val ac = activeContent
        Box.render(bounds, ab, buf)
        val inner = Box.innerRect(bounds, ab)
        if inner.width <= 0 || inner.height <= 0 then return

        val sel = selected.get
        val s = scroll.get

        var row = 0
        while row < inner.height do
          val itemIdx = s + row
          if itemIdx < items.length then
            val raw = label(items(itemIdx))
            // Truncate to inner width (character count; wide chars not accounted for).
            val text = raw.take(inner.width).padTo(inner.width, ' ')
            val itemStyle = if itemIdx == sel then ac.withInvert else ac
            buf.putString(inner.x, inner.y + row, text, itemStyle)
          row += 1
