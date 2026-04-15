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

trait Color:
  object foreground:
    def default[F, A](
        f: F ?=> A
    ): (F & effect.Color) ?=> A =
      effect ?=> effect.foreground.default(() => f(using effect))

    def black[F, A](
        f: F ?=> A
    ): (F & effect.Color) ?=> A =
      effect ?=> effect.foreground.black(() => f(using effect))

    def red[F, A](
        f: F ?=> A
    ): (F & effect.Color) ?=> A =
      effect ?=> effect.foreground.red(() => f(using effect))

    def green[F, A](
        f: F ?=> A
    ): (F & effect.Color) ?=> A =
      effect ?=> effect.foreground.green(() => f(using effect))

    def yellow[F, A](
        f: F ?=> A
    ): (F & effect.Color) ?=> A =
      effect ?=> effect.foreground.yellow(() => f(using effect))

    def blue[F, A](
        f: F ?=> A
    ): (F & effect.Color) ?=> A =
      effect ?=> effect.foreground.blue(() => f(using effect))

    def magenta[F, A](
        f: F ?=> A
    ): (F & effect.Color) ?=> A =
      effect ?=> effect.foreground.magenta(() => f(using effect))

    def cyan[F, A](
        f: F ?=> A
    ): (F & effect.Color) ?=> A =
      effect ?=> effect.foreground.cyan(() => f(using effect))

    def white[F, A](
        f: F ?=> A
    ): (F & effect.Color) ?=> A =
      effect ?=> effect.foreground.white(() => f(using effect))

    def brightBlack[F, A](
        f: F ?=> A
    ): (F & effect.Color) ?=> A =
      effect ?=> effect.foreground.brightBlack(() => f(using effect))

    def brightRed[F, A](
        f: F ?=> A
    ): (F & effect.Color) ?=> A =
      effect ?=> effect.foreground.brightRed(() => f(using effect))

    def brightGreen[F, A](
        f: F ?=> A
    ): (F & effect.Color) ?=> A =
      effect ?=> effect.foreground.brightGreen(() => f(using effect))

    def brightYellow[F, A](
        f: F ?=> A
    ): (F & effect.Color) ?=> A =
      effect ?=> effect.foreground.brightYellow(() => f(using effect))

    def brightBlue[F, A](
        f: F ?=> A
    ): (F & effect.Color) ?=> A =
      effect ?=> effect.foreground.brightBlue(() => f(using effect))

    def brightMagenta[F, A](
        f: F ?=> A
    ): (F & effect.Color) ?=> A =
      effect ?=> effect.foreground.brightMagenta(() => f(using effect))

    def brightCyan[F, A](
        f: F ?=> A
    ): (F & effect.Color) ?=> A =
      effect ?=> effect.foreground.brightCyan(() => f(using effect))

    def brightWhite[F, A](
        f: F ?=> A
    ): (F & effect.Color) ?=> A =
      effect ?=> effect.foreground.brightWhite(() => f(using effect))

  object background:
    def default[F, A](
        f: F ?=> A
    ): (F & effect.Color) ?=> A =
      effect ?=> effect.background.default(() => f(using effect))

    def black[F, A](
        f: F ?=> A
    ): (F & effect.Color) ?=> A =
      effect ?=> effect.background.black(() => f(using effect))

    def red[F, A](
        f: F ?=> A
    ): (F & effect.Color) ?=> A =
      effect ?=> effect.background.red(() => f(using effect))

    def green[F, A](
        f: F ?=> A
    ): (F & effect.Color) ?=> A =
      effect ?=> effect.background.green(() => f(using effect))

    def yellow[F, A](
        f: F ?=> A
    ): (F & effect.Color) ?=> A =
      effect ?=> effect.background.yellow(() => f(using effect))

    def blue[F, A](
        f: F ?=> A
    ): (F & effect.Color) ?=> A =
      effect ?=> effect.background.blue(() => f(using effect))

    def magenta[F, A](
        f: F ?=> A
    ): (F & effect.Color) ?=> A =
      effect ?=> effect.background.magenta(() => f(using effect))

    def cyan[F, A](
        f: F ?=> A
    ): (F & effect.Color) ?=> A =
      effect ?=> effect.background.cyan(() => f(using effect))

    def white[F, A](
        f: F ?=> A
    ): (F & effect.Color) ?=> A =
      effect ?=> effect.background.white(() => f(using effect))

    def brightBlack[F, A](
        f: F ?=> A
    ): (F & effect.Color) ?=> A =
      effect ?=> effect.background.brightBlack(() => f(using effect))

    def brightRed[F, A](
        f: F ?=> A
    ): (F & effect.Color) ?=> A =
      effect ?=> effect.background.brightRed(() => f(using effect))

    def brightGreen[F, A](
        f: F ?=> A
    ): (F & effect.Color) ?=> A =
      effect ?=> effect.background.brightGreen(() => f(using effect))

    def brightYellow[F, A](
        f: F ?=> A
    ): (F & effect.Color) ?=> A =
      effect ?=> effect.background.brightYellow(() => f(using effect))

    def brightBlue[F, A](
        f: F ?=> A
    ): (F & effect.Color) ?=> A =
      effect ?=> effect.background.brightBlue(() => f(using effect))

    def brightMagenta[F, A](
        f: F ?=> A
    ): (F & effect.Color) ?=> A =
      effect ?=> effect.background.brightMagenta(() => f(using effect))

    def brightCyan[F, A](
        f: F ?=> A
    ): (F & effect.Color) ?=> A =
      effect ?=> effect.background.brightCyan(() => f(using effect))

    def brightWhite[F, A](
        f: F ?=> A
    ): (F & effect.Color) ?=> A =
      effect ?=> effect.background.brightWhite(() => f(using effect))
