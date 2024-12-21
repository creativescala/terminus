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

package terminus

trait Color {
  object foreground {
    def default[F <: effect.Writer, A](
        f: F ?=> A
    ): (F & effect.Color[F]) ?=> A =
      effect ?=> effect.foreground.default(f)

    def black[F <: effect.Writer, A](
        f: F ?=> A
    ): (F & effect.Color[F]) ?=> A =
      effect ?=> effect.foreground.black(f)

    def red[F <: effect.Writer, A](
        f: F ?=> A
    ): (F & effect.Color[F]) ?=> A =
      effect ?=> effect.foreground.red(f)

    def green[F <: effect.Writer, A](
        f: F ?=> A
    ): (F & effect.Color[F]) ?=> A =
      effect ?=> effect.foreground.green(f)

    def yellow[F <: effect.Writer, A](
        f: F ?=> A
    ): (F & effect.Color[F]) ?=> A =
      effect ?=> effect.foreground.yellow(f)

    def blue[F <: effect.Writer, A](
        f: F ?=> A
    ): (F & effect.Color[F]) ?=> A =
      effect ?=> effect.foreground.blue(f)

    def magenta[F <: effect.Writer, A](
        f: F ?=> A
    ): (F & effect.Color[F]) ?=> A =
      effect ?=> effect.foreground.magenta(f)

    def cyan[F <: effect.Writer, A](
        f: F ?=> A
    ): (F & effect.Color[F]) ?=> A =
      effect ?=> effect.foreground.cyan(f)

    def white[F <: effect.Writer, A](
        f: F ?=> A
    ): (F & effect.Color[F]) ?=> A =
      effect ?=> effect.foreground.white(f)

    def brightBlack[F <: effect.Writer, A](
        f: F ?=> A
    ): (F & effect.Color[F]) ?=> A =
      effect ?=> effect.foreground.brightBlack(f)

    def brightRed[F <: effect.Writer, A](
        f: F ?=> A
    ): (F & effect.Color[F]) ?=> A =
      effect ?=> effect.foreground.brightRed(f)

    def brightGreen[F <: effect.Writer, A](
        f: F ?=> A
    ): (F & effect.Color[F]) ?=> A =
      effect ?=> effect.foreground.brightGreen(f)

    def brightYellow[F <: effect.Writer, A](
        f: F ?=> A
    ): (F & effect.Color[F]) ?=> A =
      effect ?=> effect.foreground.brightYellow(f)

    def brightBlue[F <: effect.Writer, A](
        f: F ?=> A
    ): (F & effect.Color[F]) ?=> A =
      effect ?=> effect.foreground.brightBlue(f)

    def brightMagenta[F <: effect.Writer, A](
        f: F ?=> A
    ): (F & effect.Color[F]) ?=> A =
      effect ?=> effect.foreground.brightMagenta(f)

    def brightCyan[F <: effect.Writer, A](
        f: F ?=> A
    ): (F & effect.Color[F]) ?=> A =
      effect ?=> effect.foreground.brightCyan(f)

    def brightWhite[F <: effect.Writer, A](
        f: F ?=> A
    ): (F & effect.Color[F]) ?=> A =
      effect ?=> effect.foreground.brightWhite(f)
  }

  object background {
    def default[F <: effect.Writer, A](
        f: F ?=> A
    ): (F & effect.Color[F]) ?=> A =
      effect ?=> effect.background.default(f)

    def black[F <: effect.Writer, A](
        f: F ?=> A
    ): (F & effect.Color[F]) ?=> A =
      effect ?=> effect.background.black(f)

    def red[F <: effect.Writer, A](
        f: F ?=> A
    ): (F & effect.Color[F]) ?=> A =
      effect ?=> effect.background.red(f)

    def green[F <: effect.Writer, A](
        f: F ?=> A
    ): (F & effect.Color[F]) ?=> A =
      effect ?=> effect.background.green(f)

    def yellow[F <: effect.Writer, A](
        f: F ?=> A
    ): (F & effect.Color[F]) ?=> A =
      effect ?=> effect.background.yellow(f)

    def blue[F <: effect.Writer, A](
        f: F ?=> A
    ): (F & effect.Color[F]) ?=> A =
      effect ?=> effect.background.blue(f)

    def magenta[F <: effect.Writer, A](
        f: F ?=> A
    ): (F & effect.Color[F]) ?=> A =
      effect ?=> effect.background.magenta(f)

    def cyan[F <: effect.Writer, A](
        f: F ?=> A
    ): (F & effect.Color[F]) ?=> A =
      effect ?=> effect.background.cyan(f)

    def white[F <: effect.Writer, A](
        f: F ?=> A
    ): (F & effect.Color[F]) ?=> A =
      effect ?=> effect.background.white(f)

    def brightBlack[F <: effect.Writer, A](
        f: F ?=> A
    ): (F & effect.Color[F]) ?=> A =
      effect ?=> effect.background.brightBlack(f)

    def brightRed[F <: effect.Writer, A](
        f: F ?=> A
    ): (F & effect.Color[F]) ?=> A =
      effect ?=> effect.background.brightRed(f)

    def brightGreen[F <: effect.Writer, A](
        f: F ?=> A
    ): (F & effect.Color[F]) ?=> A =
      effect ?=> effect.background.brightGreen(f)

    def brightYellow[F <: effect.Writer, A](
        f: F ?=> A
    ): (F & effect.Color[F]) ?=> A =
      effect ?=> effect.background.brightYellow(f)

    def brightBlue[F <: effect.Writer, A](
        f: F ?=> A
    ): (F & effect.Color[F]) ?=> A =
      effect ?=> effect.background.brightBlue(f)

    def brightMagenta[F <: effect.Writer, A](
        f: F ?=> A
    ): (F & effect.Color[F]) ?=> A =
      effect ?=> effect.background.brightMagenta(f)

    def brightCyan[F <: effect.Writer, A](
        f: F ?=> A
    ): (F & effect.Color[F]) ?=> A =
      effect ?=> effect.background.brightCyan(f)

    def brightWhite[F <: effect.Writer, A](
        f: F ?=> A
    ): (F & effect.Color[F]) ?=> A =
      effect ?=> effect.background.brightWhite(f)
  }
}
