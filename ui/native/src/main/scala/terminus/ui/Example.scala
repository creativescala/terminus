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

import terminus.NativeTerminal
import terminus.ui.component.Text
import terminus.ui.style.Color
import terminus.ui.style.Style
import terminus.ui.style.Underline

// Run with: sbt 'uiNative/runMain terminus.ui.demo'
@main def demo(): Unit =
  val program: FullScreen.Program[Unit] =
    FullScreen {

      // Row 1: text style attributes
      Row {
        Text(24, 3, Style(bold = true))(
          "Bold 💪"
        )
        Text(24, 3, Style(italic = true))(
          "Italic ✨"
        )
        Text(24, 3, Style(strikethrough = true))(
          "Strikethrough ❌"
        )
      }

      // Row 2: underline variants and invert
      Row {
        Text(24, 3, Style(underline = Underline.Straight))(
          "Straight underline"
        )
        Text(24, 3, Style(underline = Underline.Curly))(
          "Curly underline"
        )
        Text(24, 3, Style(invert = true))(
          "Inverted 🔄"
        )
      }

      // Column nested inside a Row: demonstrates Column layout
      Row {
        Column {
          Text(24, 3, Style(fg = Color.Red, bold = true))(
            "🔴 Red — 红色"
          )
          Text(24, 3, Style(fg = Color.Green, bold = true))(
            "🟢 Green — 緑"
          )
          Text(24, 3, Style(fg = Color.Blue, bold = true))(
            "🔵 Blue — 青色"
          )
        }
        Text(24, 9, Style(fg = Color.Yellow))(
          "Column on the left\nhas three coloured\nrows stacked."
        )
      }
    }

  NativeTerminal.run {
    program
    Terminal.newline
  }
