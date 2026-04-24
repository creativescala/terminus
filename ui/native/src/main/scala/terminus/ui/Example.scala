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

package terminus.ui

import terminus.Key
import terminus.NativeTerminal
import terminus.ui.component.Text
import terminus.ui.style.Color
import terminus.ui.style.ComponentStyle
import terminus.ui.style.Style
import terminus.ui.style.Underline

// Run with: sbt 'uiNative/runMain terminus.ui.demo'
@main def demo(): Unit =
  val program: FullScreen.Program[Unit] =
    FullScreen {

      // Row 1: text style attributes
      Row {
        Text(24, 3, content = Style(bold = true))(
          "Bold 💪"
        )
        Text(24, 3, content = Style(italic = true))(
          "Italic ✨"
        )
        Text(24, 3, content = Style(strikethrough = true))(
          "Strikethrough ❌"
        )
      }

      // Row 2: underline variants and invert
      Row {
        Text(24, 3, content = Style(underline = Underline.Straight))(
          "Straight underline"
        )
        Text(24, 3, content = Style(underline = Underline.Curly))(
          "Curly underline"
        )
        Text(24, 3, content = Style(invert = true))(
          "Inverted 🔄"
        )
      }

      // Row 3: component styling — coloured borders and background fill
      Row {
        Column {
          Text(
            24,
            3,
            box = ComponentStyle(borderStyle = Style(fg = Color.Red)),
            content = Style(fg = Color.Red, bold = true)
          )("🔴 Red — 红色")
          Text(
            24,
            3,
            box = ComponentStyle(borderStyle = Style(fg = Color.Green)),
            content = Style(fg = Color.Green, bold = true)
          )("🟢 Green — 緑")
          Text(
            24,
            3,
            box = ComponentStyle(borderStyle = Style(fg = Color.Blue)),
            content = Style(fg = Color.Blue, bold = true)
          )("🔵 Blue — 青色")
        }
        Text(
          24,
          9,
          box = ComponentStyle(
            background = Style(bg = Color.Yellow),
            borderStyle = Style(fg = Color.BrightBlack)
          ),
          content = Style(fg = Color.Black, bg = Color.Yellow)
        )("Column on the left\nhas coloured\nborders.")
      }
    }

  NativeTerminal.run {
    program
    Terminal.newline
  }

// Run with: sbt 'uiNative/runMain terminus.ui.interactiveDemo'
@main def interactiveDemo(): Unit =
  val program: FullScreen.InteractiveProgram[Unit] =
    FullScreen.run { ctx ?=>
      val countA = ctx.createSignal(0)
      val countB = ctx.createSignal(0)

      ctx.onKey(Key('q')) { ctx.stop() }
      ctx.onKey(Key.controlC) { ctx.stop() }

      Column {
        Text(50)("Tab to switch focus, ↑/↓ to change, q to quit")

        FocusScope { ctx ?=>
          ctx.onKey(Key.up) { countA.update(_ + 1) }
          ctx.onKey(Key.down) { countA.update(_ - 1) }
          Text(50) {
            val focused = ctx.isFocused
            val count = countA.get
            val footer =
              if count == 0 then ""
              else if count < 0 then "\n  Negative"
              else "\n  Positive"
            s"""${if focused then "▶ " else "  "}Counter A: ${count}${footer}"""
          }
        }

        FocusScope { ctx ?=>
          ctx.onKey(Key.up) { countB.update(_ + 1) }
          ctx.onKey(Key.down) { countB.update(_ - 1) }
          Text(50) {
            val focused = ctx.isFocused
            val count = countB.get
            val footer =
              if count == 0 then ""
              else if count < 0 then "\n  Negative"
              else "\n  Positive"
            s"${if focused then "▶ " else "  "}Counter B: ${count}${footer}"
          }
        }
      }
    }

  NativeTerminal.run(program)
