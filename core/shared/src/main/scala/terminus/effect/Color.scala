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

trait Color[+F <: Writer] extends Sgr[F] { self: F =>
  object foreground {
    def default[A](f: F ?=> A): A =
      withColor("39")(f)

    def black[A](f: F ?=> A): A =
      withColor("30")(f)

    def red[A](f: F ?=> A): A =
      withColor("31")(f)

    def green[A](f: F ?=> A): A =
      withColor("32")(f)

    def yellow[A](f: F ?=> A): A =
      withColor("33")(f)

    def blue[A](f: F ?=> A): A =
      withColor("34")(f)

    def magenta[A](f: F ?=> A): A =
      withColor("35")(f)

    def cyan[A](f: F ?=> A): A =
      withColor("36")(f)

    def white[A](f: F ?=> A): A =
      withColor("37")(f)

    def brightBlack[A](f: F ?=> A): A =
      withColor("90")(f)

    def brightRed[A](f: F ?=> A): A =
      withColor("91")(f)

    def brightGreen[A](f: F ?=> A): A =
      withColor("92")(f)

    def brightYellow[A](f: F ?=> A): A =
      withColor("93")(f)

    def brightBlue[A](f: F ?=> A): A =
      withColor("94")(f)
    def brightMagenta[A](f: F ?=> A): A =
      withColor("95")(f)

    def brightCyan[A](f: F ?=> A): A =
      withColor("96")(f)

    def brightWhite[A](f: F ?=> A): A =
      withColor("97")(f)
  }

  object background {
    def default[A](f: F ?=> A): A =
      withColor("49")(f)

    def black[A](f: F ?=> A): A =
      withColor("40")(f)

    def red[A](f: F ?=> A): A =
      withColor("41")(f)

    def green[A](f: F ?=> A): A =
      withColor("42")(f)

    def yellow[A](f: F ?=> A): A =
      withColor("43")(f)

    def blue[A](f: F ?=> A): A =
      withColor("44")(f)

    def magenta[A](f: F ?=> A): A =
      withColor("45")(f)

    def cyan[A](f: F ?=> A): A =
      withColor("46")(f)

    def white[A](f: F ?=> A): A =
      withColor("47")(f)

    def brightBlack[A](f: F ?=> A): A =
      withColor("100")(f)

    def brightRed[A](f: F ?=> A): A =
      withColor("101")(f)

    def brightGreen[A](f: F ?=> A): A =
      withColor("102")(f)

    def brightYellow[A](f: F ?=> A): A =
      withColor("103")(f)

    def brightBlue[A](f: F ?=> A): A =
      withColor("104")(f)

    def brightMagenta[A](f: F ?=> A): A =
      withColor("105")(f)

    def brightCyan[A](f: F ?=> A): A =
      withColor("106")(f)

    def brightWhite[A](f: F ?=> A): A =
      withColor("107")(f)
  }

  private def withColor[A](code: String)(f: F ?=> A): A = {
    sgr(code)
    val result = f(using this)
    reset()

    result
  }
}
