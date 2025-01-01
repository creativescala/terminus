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
      withEffect("39")(f)

    def black[A](f: F ?=> A): A =
      withEffect("30")(f)

    def red[A](f: F ?=> A): A =
      withEffect("31")(f)

    def green[A](f: F ?=> A): A =
      withEffect("32")(f)

    def yellow[A](f: F ?=> A): A =
      withEffect("33")(f)

    def blue[A](f: F ?=> A): A =
      withEffect("34")(f)

    def magenta[A](f: F ?=> A): A =
      withEffect("35")(f)

    def cyan[A](f: F ?=> A): A =
      withEffect("36")(f)

    def white[A](f: F ?=> A): A =
      withEffect("37")(f)

    def brightBlack[A](f: F ?=> A): A =
      withEffect("90")(f)

    def brightRed[A](f: F ?=> A): A =
      withEffect("91")(f)

    def brightGreen[A](f: F ?=> A): A =
      withEffect("92")(f)

    def brightYellow[A](f: F ?=> A): A =
      withEffect("93")(f)

    def brightBlue[A](f: F ?=> A): A =
      withEffect("94")(f)
    def brightMagenta[A](f: F ?=> A): A =
      withEffect("95")(f)

    def brightCyan[A](f: F ?=> A): A =
      withEffect("96")(f)

    def brightWhite[A](f: F ?=> A): A =
      withEffect("97")(f)
  }

  object background {
    def default[A](f: F ?=> A): A =
      withEffect("49")(f)

    def black[A](f: F ?=> A): A =
      withEffect("40")(f)

    def red[A](f: F ?=> A): A =
      withEffect("41")(f)

    def green[A](f: F ?=> A): A =
      withEffect("42")(f)

    def yellow[A](f: F ?=> A): A =
      withEffect("43")(f)

    def blue[A](f: F ?=> A): A =
      withEffect("44")(f)

    def magenta[A](f: F ?=> A): A =
      withEffect("45")(f)

    def cyan[A](f: F ?=> A): A =
      withEffect("46")(f)

    def white[A](f: F ?=> A): A =
      withEffect("47")(f)

    def brightBlack[A](f: F ?=> A): A =
      withEffect("100")(f)

    def brightRed[A](f: F ?=> A): A =
      withEffect("101")(f)

    def brightGreen[A](f: F ?=> A): A =
      withEffect("102")(f)

    def brightYellow[A](f: F ?=> A): A =
      withEffect("103")(f)

    def brightBlue[A](f: F ?=> A): A =
      withEffect("104")(f)

    def brightMagenta[A](f: F ?=> A): A =
      withEffect("105")(f)

    def brightCyan[A](f: F ?=> A): A =
      withEffect("106")(f)

    def brightWhite[A](f: F ?=> A): A =
      withEffect("107")(f)
  }
}
