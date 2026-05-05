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
import terminus.Terminal
import terminus.ui.component.Select
import terminus.ui.component.Text
import terminus.ui.component.TextInput
import terminus.ui.style.Border
import terminus.ui.style.CellStyle
import terminus.ui.style.Color
import terminus.ui.style.TextStyle
import terminus.ui.style.Underline

// To build any of these examples, give sbt the command 'nativeLink'. Sbt will
// then prompt for the demo to build. The executable will
// 'ui/native/target/scala-<version>/terminus-ui'

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

  Terminal.run {
    program
    Terminal.newline
  }

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
          Text(50)(_ => focusableBox) {
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
          Text(50)(_ => focusableBox) {
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

  Terminal.run(program)

@main def textInputDemo(): Unit =
  val inputStyle = TextStyle.default
    .withBox(_.withBorderStyle(CellStyle(fg = Color.BrightBlack)))
    .withFocus(
      _.withBox(_.withBorderStyle(CellStyle(fg = Color.White, bold = true)))
    )

  val program: FullScreen.InteractiveProgram[Unit] =
    FullScreen.run { ctx ?=>
      val name = ctx.createSignal("")
      val greeting = ctx.createSignal("")

      ctx.onKey(Key('q')) { ctx.stop() }
      ctx.onKey(Key.controlC) { ctx.stop() }
      ctx.onKey(Key.enter) { greeting.set(name.peek) }
      ctx.onKey(Key.newLine) { greeting.set(name.peek) }

      Column {
        Text(50, 1)(_.withBox(_.withoutBorder))(
          "Type a name and press Enter. q to quit."
        )
        FocusScope {
          TextInput(50, name, inputStyle)
        }
        Text(50)(_.withBox(_.withoutBorder)) {
          val g = greeting.get
          if g.isEmpty then "" else s"Hello, $g!"
        }
      }
    }

  Terminal.run(program)

@main def selectDemo(): Unit =
  val fruits = Vector(
    "Apple",
    "Banana",
    "Cherry",
    "Date",
    "Elderberry",
    "Fig",
    "Grape",
    "Honeydew",
    "Kiwi",
    "Lemon",
    "Mango",
    "Nectarine",
    "Orange",
    "Papaya",
    "Quince"
  )

  val listStyle = TextStyle.default
    .withBox(_.withBorderStyle(_.withForeground(Color.BrightBlack)))
    .withFocus(
      _.withBox(_.withBorderStyle(_.withForeground(Color.White).withBold))
    )

  val program: FullScreen.InteractiveProgram[Unit] =
    FullScreen.run { ctx ?=>
      val choice = ctx.createSignal(0)

      ctx.onKey(Key('q')) { ctx.stop() }
      ctx.onKey(Key.controlC) { ctx.stop() }

      Column {
        Text(30, 1)(_.withBox(_.withoutBorder))("Pick a fruit. q to quit.")
        FocusScope { _ ?=>
          Select(30, 8, fruits, choice, listStyle)
        }
        Text(30)(_.withBox(_.withoutBorder)) {
          s"Selected: ${fruits(choice.get)}"
        }
      }
    }

  Terminal.run(program)

@main def terminalDimensionsDemo(): Unit =
  Terminal.run {
    val dimensions = Terminal.dimensions.get
    Terminal.write(s"Dimensions: $dimensions\n")
    Terminal.flush()
  }
