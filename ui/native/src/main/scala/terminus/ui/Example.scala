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
import terminus.ui.component.Column
import terminus.ui.component.Row
import terminus.ui.component.Text
import terminus.ui.component.TextInput
import terminus.ui.layout.Size
import terminus.ui.react.Reactive
import terminus.ui.react.Var
import terminus.ui.style.CellStyle
import terminus.ui.style.Color
import terminus.ui.style.TextStyle
import terminus.ui.style.Underline

// To build any of these examples, give sbt the command 'nativeLink'. Sbt will
// then prompt for the demo to build. The executable will
// 'ui/native/target/scala-<version>/terminus-ui'

/** Wrap unchanging text as a [[terminus.ui.react.Reactive]], which is what
  * [[Text]] requires as content.
  */
private def staticText(s: String) = Var(text.Text(s))

@main def demo(): Unit =
  val fullScreen = FullScreen {

    // Row 1: text style attributes
    Row(Size.fixed(72, 3)) {
      Text(Size.fixed(24, 3), _.withContent(_.withBold)) {
        staticText("Bold 💪")
      }
      Text(Size.fixed(24, 3), _.withContent(_.withItalic)) {
        staticText("Italic ✨")
      }
      Text(Size.fixed(24, 3), _.withContent(_.withStrikethrough)) {
        staticText("Strikethrough ❌")
      }
    }

    // Row 2: underline variants and invert
    Row(Size.fixed(72, 3)) {
      Text(
        Size.fixed(24, 3),
        _.withContent(_.withUnderline(Underline.Straight))
      ) {
        staticText("Straight underline")
      }
      Text(Size.fixed(24, 3), _.withContent(_.withUnderline(Underline.Curly))) {
        staticText("Curly underline")
      }
      Text(Size.fixed(24, 3), _.withContent(_.withInvert)) {
        staticText("Inverted 🔄")
      }
    }

    // Row 3: component styling — coloured borders and background fill
    Row(Size.fixed(48, 9)) {
      Column(Size.fixed(24, 9)) {
        Text(
          Size.fixed(24, 3),
          _.withBox(_.withBorderStyle(CellStyle(fg = Color.Red)))
            .withContent(CellStyle(fg = Color.Red, bold = true))
        ) { staticText("🔴 Red — 红色") }
        Text(
          Size.fixed(24, 3),
          _.withBox(_.withBorderStyle(CellStyle(fg = Color.Green)))
            .withContent(CellStyle(fg = Color.Green, bold = true))
        ) { staticText("🟢 Green — 緑") }
        Text(
          Size.fixed(24, 3),
          _.withBox(_.withBorderStyle(CellStyle(fg = Color.Blue)))
            .withContent(CellStyle(fg = Color.Blue, bold = true))
        ) { staticText("🔵 Blue — 青色") }
      }
      Text(
        Size.fixed(24, 9),
        _.withBox(_.withBorderStyle(CellStyle(fg = Color.Yellow)))
          .withContent(CellStyle(fg = Color.Yellow, bold = true))
      ) { staticText("Column on the left\nhas coloured\nborders.") }
    }
  }

  // FullScreen always runs the full interactive loop (alternate screen, raw
  // mode, key-read loop) — there is no longer a render-once mode. Ctrl+Q quits.
  fullScreen.run(NativeTerminal)

// TODO: port to the new component APIs. No longer blocked on FocusScope (not
// needed — each component is independently focusable) or Runtime focus
// registration (fixed). Just needs rewriting: the old ctx.createSignal/
// ctx.onKey/ctx.stop() API doesn't exist; use Var for the counters and
// register onKey directly in each Column's body (Event & Layout ?=> Unit),
// which makes that Column focusable and part of the tab order.
@main def interactiveDemo(): Unit = ???

@main def textInputDemo(): Unit =
  val inputStyle = TextStyle.default
    .withBox(_.withBorderStyle(CellStyle(fg = Color.BrightBlack)))
    .withFocus(
      _.withBox(_.withBorderStyle(CellStyle(fg = Color.White, bold = true)))
    )

  val fullScreen = FullScreen {
    val name = Var(text.Line(""))

    Column(Size.fixed(50, 5)) {
      Text(Size.fixed(50, 1), _.withBox(_.withoutBorder)) {
        staticText("Type a name. Ctrl+Q to quit.")
      }
      TextInput(Size.fixed(50, 3), _ => inputStyle, name)
      Text(Size.fixed(50, 1), _.withBox(_.withoutBorder)) {
        Reactive {
          val typed = name.get.value
          text.Text(if typed.isEmpty then "" else s"Hello, $typed!")
        }
      }
    }
  }

  fullScreen.run(NativeTerminal)

// TODO: port to the new component APIs (Select already has apply — this one
// is no longer blocked, just not yet rewritten).
@main def selectDemo(): Unit = ???
