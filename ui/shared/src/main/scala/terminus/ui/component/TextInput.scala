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
import terminus.KeyCode
import terminus.ui.AppContext
import terminus.ui.Buffer
import terminus.ui.Cell
import terminus.ui.Component
import terminus.ui.Rect
import terminus.ui.RenderContext
import terminus.ui.Signal
import terminus.ui.Size
import terminus.ui.style.TextStyle
import terminus.ui.tool.Box

object TextInput:

  /** Add a single-line text input component.
    *
    * The component must be placed inside a [[terminus.ui.FocusScope]] to
    * receive keyboard input. Call [[terminus.ui.AppContext.createSignal]] to
    * create `value` before calling this method:
    *
    * {{{
    * FocusScope { ctx ?=>
    *   val name = ctx.createSignal("")
    *   TextInput(30, name)
    * }
    * }}}
    *
    * Key bindings: printable characters insert at the cursor, Backspace / Delete
    * delete around the cursor, Left / Right / Home / End move the cursor. Text
    * longer than the visible area scrolls horizontally to keep the cursor in
    * view.
    */
  def apply(
      width: Int,
      value: Signal[String],
      style: TextStyle = TextStyle.default
  )(using ctx: AppContext): Unit =
    val cursor = ctx.createSignal(0)

    ctx.onKey(Key.left) {
      cursor.update(c => (c - 1).max(0))
    }
    ctx.onKey(Key.right) {
      cursor.update(c => (c + 1).min(value.peek.length))
    }
    ctx.onKey(Key.home) {
      cursor.set(0)
    }
    ctx.onKey(Key.`end`) {
      cursor.set(value.peek.length)
    }
    ctx.onKey(Key.backspace) {
      val text = value.peek
      val pos = cursor.peek
      if pos > 0 then
        value.set(text.substring(0, pos - 1) + text.substring(pos))
        cursor.update(_ - 1)
    }
    ctx.onKey(Key.delete) {
      val text = value.peek
      val pos = cursor.peek
      if pos < text.length then
        value.set(text.substring(0, pos) + text.substring(pos + 1))
    }
    ctx.onAnyKey { key =>
      key.code match
        case KeyCode.Character(c) if !c.isControl =>
          val text = value.peek
          val pos = cursor.peek
          value.set(text.substring(0, pos) + c.toString + text.substring(pos))
          cursor.update(_ + 1)
        case _ => ()
    }

    ctx.add(component(width, value, cursor, style)(using ctx))

  private def component(
      width: Int,
      value: Signal[String],
      cursor: Signal[Int],
      style: TextStyle
  )(using rc: RenderContext): Component =
    new Component:
      private def activeBox =
        if rc.isFocused then style.focus.map(_.box).getOrElse(style.box)
        else style.box
      private def activeContent =
        if rc.isFocused then style.focus.map(_.content).getOrElse(style.content)
        else style.content

      def size: Size =
        val ab = activeBox
        val offset = (if ab.border.isDefined then 1 else 0) + ab.padding
        Size(width, 1 + 2 * offset)

      def render(bounds: Rect, buf: Buffer): Unit =
        val ab = activeBox
        val ac = activeContent
        Box.render(bounds, ab, buf)
        val inner = Box.innerRect(bounds, ab)
        if inner.width <= 0 then return

        val text = value.get
        val pos = cursor.get

        // Scroll to keep the cursor in view: cursor sits at the right edge when
        // the text overflows, and at its natural position otherwise.
        val scroll = (pos - inner.width + 1).max(0)
        val visible = text.substring(scroll, (scroll + inner.width).min(text.length))
        val cursorCol = pos - scroll // always in [0, inner.width)

        buf.putString(inner.x, inner.y, visible, ac)

        // Draw the cursor by inverting the cell at the cursor column.
        val cursorChar =
          if cursorCol < visible.length then visible(cursorCol) else ' '
        buf.put(inner.x + cursorCol, inner.y, Cell(cursorChar.toInt, ac.withInvert))
