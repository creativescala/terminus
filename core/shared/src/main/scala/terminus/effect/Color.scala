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

package terminus.effect

trait Color[+F <: Writer] extends WithEffect[F] { self: F =>
  object foreground {
    def default[A](f: F ?=> A): A =
      withEffect(AnsiCodes.foreground.default)(f)

    def black[A](f: F ?=> A): A =
      withEffect(AnsiCodes.foreground.black)(f)

    def red[A](f: F ?=> A): A =
      withEffect(AnsiCodes.foreground.red)(f)

    def green[A](f: F ?=> A): A =
      withEffect(AnsiCodes.foreground.green)(f)

    def yellow[A](f: F ?=> A): A =
      withEffect(AnsiCodes.foreground.yellow)(f)

    def blue[A](f: F ?=> A): A =
      withEffect(AnsiCodes.foreground.blue)(f)

    def magenta[A](f: F ?=> A): A =
      withEffect(AnsiCodes.foreground.magenta)(f)

    def cyan[A](f: F ?=> A): A =
      withEffect(AnsiCodes.foreground.cyan)(f)

    def white[A](f: F ?=> A): A =
      withEffect(AnsiCodes.foreground.white)(f)

    def brightBlack[A](f: F ?=> A): A =
      withEffect(AnsiCodes.foreground.brightBlack)(f)

    def brightRed[A](f: F ?=> A): A =
      withEffect(AnsiCodes.foreground.brightRed)(f)

    def brightGreen[A](f: F ?=> A): A =
      withEffect(AnsiCodes.foreground.brightGreen)(f)

    def brightYellow[A](f: F ?=> A): A =
      withEffect(AnsiCodes.foreground.brightYellow)(f)

    def brightBlue[A](f: F ?=> A): A =
      withEffect(AnsiCodes.foreground.brightBlue)(f)
    def brightMagenta[A](f: F ?=> A): A =
      withEffect(AnsiCodes.foreground.brightMagenta)(f)

    def brightCyan[A](f: F ?=> A): A =
      withEffect(AnsiCodes.foreground.brightCyan)(f)

    def brightWhite[A](f: F ?=> A): A =
      withEffect(AnsiCodes.foreground.brightWhite)(f)
  }

  object background {
    def default[A](f: F ?=> A): A =
      withEffect(AnsiCodes.background.default)(f)

    def black[A](f: F ?=> A): A =
      withEffect(AnsiCodes.background.black)(f)

    def red[A](f: F ?=> A): A =
      withEffect(AnsiCodes.background.red)(f)

    def green[A](f: F ?=> A): A =
      withEffect(AnsiCodes.background.green)(f)

    def yellow[A](f: F ?=> A): A =
      withEffect(AnsiCodes.background.yellow)(f)

    def blue[A](f: F ?=> A): A =
      withEffect(AnsiCodes.background.blue)(f)

    def magenta[A](f: F ?=> A): A =
      withEffect(AnsiCodes.background.magenta)(f)

    def cyan[A](f: F ?=> A): A =
      withEffect(AnsiCodes.background.cyan)(f)

    def white[A](f: F ?=> A): A =
      withEffect(AnsiCodes.background.white)(f)

    def brightBlack[A](f: F ?=> A): A =
      withEffect(AnsiCodes.background.brightBlack)(f)

    def brightRed[A](f: F ?=> A): A =
      withEffect(AnsiCodes.background.brightRed)(f)

    def brightGreen[A](f: F ?=> A): A =
      withEffect(AnsiCodes.background.brightGreen)(f)

    def brightYellow[A](f: F ?=> A): A =
      withEffect(AnsiCodes.background.brightYellow)(f)

    def brightBlue[A](f: F ?=> A): A =
      withEffect(AnsiCodes.background.brightBlue)(f)

    def brightMagenta[A](f: F ?=> A): A =
      withEffect(AnsiCodes.background.brightMagenta)(f)

    def brightCyan[A](f: F ?=> A): A =
      withEffect(AnsiCodes.background.brightCyan)(f)

    def brightWhite[A](f: F ?=> A): A =
      withEffect(AnsiCodes.background.brightWhite)(f)
  }
}
