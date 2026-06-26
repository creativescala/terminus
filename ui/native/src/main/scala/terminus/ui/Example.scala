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
import terminus.ui.capability.Event
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

/** A little graphic equalizer, showing off reactive content and the layout
  * system. The arrow keys mutate plain [[Var]]s; each bar is a [[Text]] whose
  * content is a [[Reactive]] derived from those Vars, so it redraws itself when
  * they change. The whole screen is a single focusable [[Column]] that owns the
  * key handling — registering `onKey` in its body makes it focusable, and as
  * the only focusable element it receives keys immediately.
  */
@main def interactiveDemo(): Unit =
  val maxLevel = 16
  val channels = Vector(
    "Bass" -> Color.Red,
    "Low-Mid" -> Color.Yellow,
    "Mid" -> Color.Green,
    "High-Mid" -> Color.Cyan,
    "Treble" -> Color.Magenta
  )

  // levels(i) is the height of channel i's bar; selected is the channel the
  // arrow keys currently act on.
  val levels = Var(Vector.fill(channels.size)(maxLevel / 2))
  val selected = Var(0)

  val fullScreen = FullScreen {
    Column(Size.fixed(40, channels.size + 4)) {
      val events = summon[Event]
      events.onKey(Key.up) {
        selected.update(s => (s - 1 + channels.size) % channels.size)
      }
      events.onKey(Key.down) {
        selected.update(s => (s + 1) % channels.size)
      }
      events.onKey(Key.right) {
        val i = selected.peek
        levels.update(ls => ls.updated(i, (ls(i) + 1).min(maxLevel)))
      }
      events.onKey(Key.left) {
        val i = selected.peek
        levels.update(ls => ls.updated(i, (ls(i) - 1).max(0)))
      }

      Text(
        Size.fixed(40, 1),
        _.withBox(_.withoutBorder).withContent(_.withBold)
      ) {
        staticText("🎛  Equalizer")
      }

      channels.zipWithIndex.foreach { case ((name, colour), i) =>
        Row(Size.fixed(40, 1)) {
          // Channel name, with a marker on the selected channel.
          Text(Size.fixed(12, 1), _.withBox(_.withoutBorder)) {
            Reactive {
              val marker = if selected.get == i then "▶ " else "  "
              text.Text(marker + name)
            }
          }
          // The bar itself: filled blocks up to the level, light shade beyond.
          Text(
            Size.fixed(maxLevel, 1),
            _.withBox(_.withoutBorder).withContent(CellStyle(fg = colour))
          ) {
            Reactive {
              val n = levels.get.apply(i)
              text.Text(("█" * n) + ("░" * (maxLevel - n)))
            }
          }
          // Numeric value.
          Text(Size.fixed(4, 1), _.withBox(_.withoutBorder)) {
            Reactive {
              val n = levels.get.apply(i)
              text.Text(f" $n%2d")
            }
          }
        }
      }

      Text(Size.fixed(40, 1), _.withBox(_.withoutBorder)) {
        staticText("←/→ adjust · ↑/↓ pick · Ctrl+Q quit")
      }
    }
  }

  fullScreen.run(NativeTerminal)

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
