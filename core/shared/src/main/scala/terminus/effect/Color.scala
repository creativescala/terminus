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

trait Color[+F <: Writer] extends WithStack[F] { self: F =>
  object foreground {
    private val foregroundStack: Stack = Stack(AnsiCodes.foreground.default)

    def default[A](f: F ?=> A): A =
      withStack(foregroundStack, AnsiCodes.foreground.default)(f)

    def black[A](f: F ?=> A): A =
      withStack(foregroundStack, AnsiCodes.foreground.black)(f)

    def red[A](f: F ?=> A): A =
      withStack(foregroundStack, AnsiCodes.foreground.red)(f)

    def green[A](f: F ?=> A): A =
      withStack(foregroundStack, AnsiCodes.foreground.green)(f)

    def yellow[A](f: F ?=> A): A =
      withStack(foregroundStack, AnsiCodes.foreground.yellow)(f)

    def blue[A](f: F ?=> A): A =
      withStack(foregroundStack, AnsiCodes.foreground.blue)(f)

    def magenta[A](f: F ?=> A): A =
      withStack(foregroundStack, AnsiCodes.foreground.magenta)(f)

    def cyan[A](f: F ?=> A): A =
      withStack(foregroundStack, AnsiCodes.foreground.cyan)(f)

    def white[A](f: F ?=> A): A =
      withStack(foregroundStack, AnsiCodes.foreground.white)(f)

    def brightBlack[A](f: F ?=> A): A =
      withStack(foregroundStack, AnsiCodes.foreground.brightBlack)(f)

    def brightRed[A](f: F ?=> A): A =
      withStack(foregroundStack, AnsiCodes.foreground.brightRed)(f)

    def brightGreen[A](f: F ?=> A): A =
      withStack(foregroundStack, AnsiCodes.foreground.brightGreen)(f)

    def brightYellow[A](f: F ?=> A): A =
      withStack(foregroundStack, AnsiCodes.foreground.brightYellow)(f)

    def brightBlue[A](f: F ?=> A): A =
      withStack(foregroundStack, AnsiCodes.foreground.brightBlue)(f)
    def brightMagenta[A](f: F ?=> A): A =
      withStack(foregroundStack, AnsiCodes.foreground.brightMagenta)(f)

    def brightCyan[A](f: F ?=> A): A =
      withStack(foregroundStack, AnsiCodes.foreground.brightCyan)(f)

    def brightWhite[A](f: F ?=> A): A =
      withStack(foregroundStack, AnsiCodes.foreground.brightWhite)(f)
  }

  object background {
    private val backgroundStack: Stack = Stack(AnsiCodes.background.default)

    def default[A](f: F ?=> A): A =
      withStack(backgroundStack, AnsiCodes.background.default)(f)

    def black[A](f: F ?=> A): A =
      withStack(backgroundStack, AnsiCodes.background.black)(f)

    def red[A](f: F ?=> A): A =
      withStack(backgroundStack, AnsiCodes.background.red)(f)

    def green[A](f: F ?=> A): A =
      withStack(backgroundStack, AnsiCodes.background.green)(f)

    def yellow[A](f: F ?=> A): A =
      withStack(backgroundStack, AnsiCodes.background.yellow)(f)

    def blue[A](f: F ?=> A): A =
      withStack(backgroundStack, AnsiCodes.background.blue)(f)

    def magenta[A](f: F ?=> A): A =
      withStack(backgroundStack, AnsiCodes.background.magenta)(f)

    def cyan[A](f: F ?=> A): A =
      withStack(backgroundStack, AnsiCodes.background.cyan)(f)

    def white[A](f: F ?=> A): A =
      withStack(backgroundStack, AnsiCodes.background.white)(f)

    def brightBlack[A](f: F ?=> A): A =
      withStack(backgroundStack, AnsiCodes.background.brightBlack)(f)

    def brightRed[A](f: F ?=> A): A =
      withStack(backgroundStack, AnsiCodes.background.brightRed)(f)

    def brightGreen[A](f: F ?=> A): A =
      withStack(backgroundStack, AnsiCodes.background.brightGreen)(f)

    def brightYellow[A](f: F ?=> A): A =
      withStack(backgroundStack, AnsiCodes.background.brightYellow)(f)

    def brightBlue[A](f: F ?=> A): A =
      withStack(backgroundStack, AnsiCodes.background.brightBlue)(f)

    def brightMagenta[A](f: F ?=> A): A =
      withStack(backgroundStack, AnsiCodes.background.brightMagenta)(f)

    def brightCyan[A](f: F ?=> A): A =
      withStack(backgroundStack, AnsiCodes.background.brightCyan)(f)

    def brightWhite[A](f: F ?=> A): A =
      withStack(backgroundStack, AnsiCodes.background.brightWhite)(f)
  }
}
