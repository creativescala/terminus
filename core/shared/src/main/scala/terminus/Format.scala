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

trait Format {
  object format {
    def bold[F <: effect.Writer, A](f: F ?=> A): (F & effect.Format[F]) ?=> A =
      effect ?=> effect.format.bold(f)

    def light[F <: effect.Writer, A](
        f: F ?=> A
    ): (F & effect.Format[F]) ?=> A =
      effect ?=> effect.format.light(f)

    def normal[F <: effect.Writer, A](
        f: F ?=> A
    ): (F & effect.Format[F]) ?=> A =
      effect ?=> effect.format.normal(f)

    object underline {
      def none[F <: effect.Writer, A](
          f: F ?=> A
      ): (F & effect.Format[F]) ?=> A =
        effect ?=> effect.format.underline.none(f)

      def straight[F <: effect.Writer, A](
          f: F ?=> A
      ): (F & effect.Format[F]) ?=> A =
        effect ?=> effect.format.underline.straight(f)

      def double[F <: effect.Writer, A](
          f: F ?=> A
      ): (F & effect.Format[F]) ?=> A =
        effect ?=> effect.format.underline.double(f)

      def curly[F <: effect.Writer, A](
          f: F ?=> A
      ): (F & effect.Format[F]) ?=> A =
        effect ?=> effect.format.underline.curly(f)

      def dotted[F <: effect.Writer, A](
          f: F ?=> A
      ): (F & effect.Format[F]) ?=> A =
        effect ?=> effect.format.underline.dotted(f)

      def dashed[F <: effect.Writer, A](
          f: F ?=> A
      ): (F & effect.Format[F]) ?=> A =
        effect ?=> effect.format.underline.dashed(f)

      def default[F <: effect.Writer, A](
          f: F ?=> A
      ): (F & effect.Format[F]) ?=> A =
        effect ?=> effect.format.underline.default(f)

      def black[F <: effect.Writer, A](
          f: F ?=> A
      ): (F & effect.Format[F]) ?=> A =
        effect ?=> effect.format.underline.black(f)

      def red[F <: effect.Writer, A](
          f: F ?=> A
      ): (F & effect.Format[F]) ?=> A =
        effect ?=> effect.format.underline.red(f)

      def green[F <: effect.Writer, A](
          f: F ?=> A
      ): (F & effect.Format[F]) ?=> A =
        effect ?=> effect.format.underline.green(f)

      def yellow[F <: effect.Writer, A](
          f: F ?=> A
      ): (F & effect.Format[F]) ?=> A =
        effect ?=> effect.format.underline.yellow(f)

      def blue[F <: effect.Writer, A](
          f: F ?=> A
      ): (F & effect.Format[F]) ?=> A =
        effect ?=> effect.format.underline.blue(f)

      def magenta[F <: effect.Writer, A](
          f: F ?=> A
      ): (F & effect.Format[F]) ?=> A =
        effect ?=> effect.format.underline.magenta(f)

      def cyan[F <: effect.Writer, A](
          f: F ?=> A
      ): (F & effect.Format[F]) ?=> A =
        effect ?=> effect.format.underline.cyan(f)

      def white[F <: effect.Writer, A](
          f: F ?=> A
      ): (F & effect.Format[F]) ?=> A =
        effect ?=> effect.format.underline.white(f)
    }

    def blink[F <: effect.Writer, A](
        f: F ?=> A
    ): (F & effect.Format[F]) ?=> A =
      effect ?=> effect.format.blink(f)

    def invert[F <: effect.Writer, A](
        f: F ?=> A
    ): (F & effect.Format[F]) ?=> A =
      effect ?=> effect.format.invert(f)

    def invisible[F <: effect.Writer, A](
        f: F ?=> A
    ): (F & effect.Format[F]) ?=> A =
      effect ?=> effect.format.invisible(f)

    def strikethrough[F <: effect.Writer, A](
        f: F ?=> A
    ): (F & effect.Format[F]) ?=> A =
      effect ?=> effect.format.strikethrough(f)
  }
}
