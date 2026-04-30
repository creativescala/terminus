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
import terminus.ui.style.Border
import terminus.ui.style.CellStyle
import terminus.ui.style.Color
import terminus.ui.style.TextStyle
import terminus.ui.style.Underline

// Run with: sbt 'uiNative/runMain terminus.ui.demo'
@main def demo(): Unit =
  val program: FullScreen.Program[Unit] =
    FullScreen {

      // Row 1: text style attributes
      Row {
        Text(24, 3)(_.withContent(_.withBold))(
          "Bold 💪"
        )
        Text(24, 3)(_.withContent(_.withItalic))(
          "Italic ✨"
        )
        Text(24, 3)(_.withContent(_.withStrikethrough))(
          "Strikethrough ❌"
        )
      }

      // Row 2: underline variants and invert
      Row {
        Text(24, 3)(_.withContent(_.withUnderline(Underline.Straight)))(
          "Straight underline"
        )
        Text(24, 3)(_.withContent(_.withUnderline(Underline.Curly)))(
          "Curly underline"
        )
        Text(24, 3)(_.withContent(_.withInvert))(
          "Inverted 🔄"
        )
      }

      // Row 3: component styling — coloured borders and background fill
      Row {
        Column {
          Text(24, 3)(
            _.withBox(_.withBorderStyle(CellStyle(fg = Color.Red)))
              .withContent(CellStyle(fg = Color.Red, bold = true))
          )("🔴 Red — 红色")
          Text(24, 3)(
            _.withBox(_.withBorderStyle(CellStyle(fg = Color.Green)))
              .withContent(CellStyle(fg = Color.Green, bold = true))
          )("🟢 Green — 緑")
          Text(24, 3)(
            _.withBox(_.withBorderStyle(CellStyle(fg = Color.Blue)))
              .withContent(CellStyle(fg = Color.Blue, bold = true))
          )("🔵 Blue — 青色")
        }
        Text(24, 9)(
          _.withBox(_.withBorderStyle(CellStyle(fg = Color.Yellow)))
            .withContent(CellStyle(fg = Color.Yellow, bold = true))
        )("Column on the left\nhas coloured\nborders.")
      }
    }

  NativeTerminal.run {
    program
    Terminal.newline
  }

// Run with: sbt 'uiNative/runMain terminus.ui.interactiveDemo'
@main def interactiveDemo(): Unit =
  val focusableBox = TextStyle.default
    .withBox(
      _.withBorderStyle(CellStyle(fg = Color.BrightBlack))
    )
    .withFocus(
      _.withBox(_.withBorderStyle(CellStyle(fg = Color.White, bold = true)))
    )

  val program: FullScreen.InteractiveProgram[Unit] =
    FullScreen.run { ctx ?=>
      val countA = ctx.createSignal(0)
      val countB = ctx.createSignal(0)

      ctx.onKey(Key('q')) { ctx.stop() }
      ctx.onKey(Key.controlC) { ctx.stop() }

      Column {
        Text(50, 3)(_.withBox(_.withBorder(Border.empty)))(
          "Tab to switch focus, ↑/↓ to change, q to quit"
        )

        FocusScope { ctx ?=>
          ctx.onKey(Key.up) { countA.update(_ + 1) }
          ctx.onKey(Key.down) { countA.update(_ - 1) }
          Text(50, style = focusableBox) {
            val count = countA.get
            val footer =
              if count == 0 then ""
              else if count < 0 then "\nNegative"
              else "\nPositive"
            s"Counter A: ${count}${footer}"
          }
        }

        FocusScope { ctx ?=>
          ctx.onKey(Key.up) { countB.update(_ + 1) }
          ctx.onKey(Key.down) { countB.update(_ - 1) }
          Text(50, style = focusableBox) {
            val count = countB.get
            val footer =
              if count == 0 then ""
              else if count < 0 then "\nNegative"
              else "\nPositive"
            s"Counter B: ${count}${footer}"
          }
        }
      }
    }

  NativeTerminal.run(program)
