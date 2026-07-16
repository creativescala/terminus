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
import terminus.ui.component.Button
import terminus.ui.component.Column
import terminus.ui.component.Line
import terminus.ui.component.Row
import terminus.ui.component.Text
import terminus.ui.component.TextInput
import terminus.ui.layout.Measurement
import terminus.ui.layout.Size
import terminus.ui.react.Signal
import terminus.ui.style.Align
import terminus.ui.style.BoxProps
import terminus.ui.style.ButtonStyle
import terminus.ui.style.CellProps
import terminus.ui.style.Color
import terminus.ui.style.TextInputStyle
import terminus.ui.style.Underline

// To build any of these examples, give sbt the command 'nativeLink'. Sbt will
// then prompt for the demo to build. The executable will
// 'ui/native/target/scala-<version>/terminus-ui'

/** Wrap unchanging text as a [[terminus.ui.react.Signal]], which is what
  * [[Text]] requires as content.
  */
private def staticText(s: String) = Signal.constant(text.Text(s))

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
          _.withBox(_.withBorderProps(CellProps(fg = Color.Red)))
            .withContent(CellProps(fg = Color.Red, bold = true))
        ) { staticText("🔴 Red — 红色") }
        Text(
          Size.fixed(24, 3),
          _.withBox(_.withBorderProps(CellProps(fg = Color.Green)))
            .withContent(CellProps(fg = Color.Green, bold = true))
        ) { staticText("🟢 Green — 緑") }
        Text(
          Size.fixed(24, 3),
          _.withBox(_.withBorderProps(CellProps(fg = Color.Blue)))
            .withContent(CellProps(fg = Color.Blue, bold = true))
        ) { staticText("🔵 Blue — 青色") }
      }
      Text(
        Size.fixed(24, 9),
        _.withBox(_.withBorderProps(CellProps(fg = Color.Yellow)))
          .withContent(CellProps(fg = Color.Yellow, bold = true))
      ) { staticText("Column on the left\nhas coloured\nborders.") }
    }
  }

  // FullScreen always runs the full interactive loop (alternate screen, raw
  // mode, key-read loop) — there is no longer a render-once mode. Ctrl+Q quits.
  fullScreen.run(NativeTerminal)

/** A little graphic equalizer, showing off reactive content and the layout
  * system. The arrow keys mutate writable signals; each bar is a [[Text]] whose
  * content is a computed signal derived from them, so it redraws itself when
  * they change. The whole screen is a single focusable [[Column]] that owns the
  * key handling — registering `onKey` in its body makes it focusable, and as
  * the only focusable element it receives keys immediately.
  */
@main def interactiveDemo(): Unit =
  val maxLevel = 16
  // A warm-to-cool spectrum across the frequency bands, using 24-bit colours.
  val channels: Vector[(String, Color.Rgb)] = Vector(
    "Bass" -> Color.Rgb(255, 59, 48),
    "Low-Mid" -> Color.Rgb(255, 149, 0),
    "Mid" -> Color.Rgb(52, 199, 89),
    "High-Mid" -> Color.Rgb(50, 173, 230),
    "Treble" -> Color.Rgb(175, 82, 222)
  )

  // Interpolate cell `j` of a bar from a dim version of the channel colour (at
  // the left) to full brightness (at the right), giving each bar a smooth
  // 24-bit gradient along its length.
  def shade(base: Color.Rgb, j: Int): Color.Rgb =
    val t = if maxLevel <= 1 then 1.0 else j.toDouble / (maxLevel - 1)
    def lerp(channel: Int): Int = ((channel * (0.25 + 0.75 * t)).round).toInt
    Color.Rgb(lerp(base.r), lerp(base.g), lerp(base.b))

  val fullScreen = FullScreen { ctx ?=>
    // levels(i) is the height of channel i's bar; selected is the channel the
    // arrow keys currently act on. Signals are created inside the FullScreen
    // body, where the React capability is in scope.
    val levels = ctx.signal(Vector.fill(channels.size)(maxLevel / 2))
    val selected = ctx.signal(0)

    Column(Size.fixed(40, channels.size + 4)) { ctx ?=>
      ctx.onKey(Key.up) {
        selected.update(s => (s - 1 + channels.size) % channels.size)
      }
      ctx.onKey(Key.down) {
        selected.update(s => (s + 1) % channels.size)
      }
      ctx.onKey(Key.right) {
        val i = selected.peek
        levels.update(ls => ls.updated(i, (ls(i) + 1).min(maxLevel)))
      }
      ctx.onKey(Key.left) {
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
          Text(Size.fixed(12, 1), _.withBox(_.withoutBorder)) { ctx ?=>
            ctx.computed {
              val marker = if selected.get == i then "▶ " else "  "
              text.Text(marker + name)
            }
          }
          // The bar itself: one cell per level, each with its own gradient
          // colour. A cell is a filled block up to the current level, and a
          // light shade beyond, so the gradient is always faintly visible and
          // fills in solidly as the level rises.
          Row(Size.fixed(maxLevel, 1)) {
            (0 until maxLevel).foreach { j =>
              Text(
                Size.fixed(1, 1),
                _.withBox(_.withoutBorder)
                  .withContent(CellProps(fg = shade(colour, j)))
              ) { ctx ?=>
                ctx.computed {
                  val n = levels.get.apply(i)
                  text.Text(if j < n then "█" else "░")
                }
              }
            }
          }
          // Numeric value.
          Text(Size.fixed(4, 1), _.withBox(_.withoutBorder)) { ctx ?=>
            ctx.computed {
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
  val inputStyle = TextInputStyle.default
    .withBox(_.withBorderProps(CellProps(fg = Color.BrightBlack)))
    .focused(
      _.withBox(_.withBorderProps(CellProps(fg = Color.White, bold = true)))
    )

  val fullScreen = FullScreen { ctx ?=>
    val name = ctx.signal(text.Line(""))

    Column(Size.fixed(50, 5)) {
      Text(Size.fixed(50, 1), _.withBox(_.withoutBorder)) {
        staticText("Type a name. Ctrl+Q to quit.")
      }
      TextInput(Size.fixed(50, 3), _ => inputStyle, name)
      Text(Size.fixed(50, 1), _.withBox(_.withoutBorder)) { ctx ?=>
        ctx.computed {
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

@main def buttonDemo(): Unit =
  val activeBorder: BoxProps => BoxProps =
    s => s.withBorderProps(_.withForeground(Color.White))
  val focusedBorder: BoxProps => BoxProps =
    s => s.withBorderProps(_.withForeground(Color.Blue))

  val buttonStyle = ButtonStyle.default
    .withBox(activeBorder)
    .focused(
      _.withBox(focusedBorder)
        .withContent(_.withForeground(Color.Blue))
    )
    .disabled(
      _.withBox(_.withBorderProps(_.withForeground(Color.BrightBlack)))
        .withContent(_.withForeground(Color.BrightBlack))
    )

  // Align.Start on the root lets the column below take its natural width
  // (the width of the input + button row) instead of being stretched to the
  // full terminal width. The column's own default Align.Stretch then widens
  // the Line to match.
  val app = FullScreen.withLayout(_.withAlign(Align.Start)) { ctx ?=>
    val action = ctx.signal(text.Line(""))
    val enabled = action.map(_.isNonEmpty)
    val output = ctx.signal(text.Line.empty)

    Column(Size.wrapContent) {
      Row(Size.wrapContent) {
        TextInput(
          Size(Measurement.Fixed(25), Measurement.WrapContent),
          _.withBox(activeBorder)
            .withCursor(_.withoutInvert.withUnderline(Underline.Straight))
            .focused(_.withBox(focusedBorder)),
          action
        )

        Button(Size.wrapContent, _ => buttonStyle) { ctx ?=>
          ctx.enabledWhen(enabled)
          ctx.onSubmit {
            val a = action.peek
            action.set(text.Line.empty)
            output.set(a)
          }
          ctx.constant(text.Line("< Go >"))
        }
      }

      Line(Size.wrapContent, identity) { ctx ?=>
        output
      }
    }
  }

  app.run(NativeTerminal)
