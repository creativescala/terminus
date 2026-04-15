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

trait Format:
  object format:
    def bold[F, A](f: F ?=> A): (F & effect.Format) ?=> A =
      effect ?=> effect.format.bold(() => f(using effect))

    def light[F, A](
        f: F ?=> A
    ): (F & effect.Format) ?=> A =
      effect ?=> effect.format.light(() => f(using effect))

    def normal[F, A](
        f: F ?=> A
    ): (F & effect.Format) ?=> A =
      effect ?=> effect.format.normal(() => f(using effect))

    object underline:
      def none[F, A](
          f: F ?=> A
      ): (F & effect.Format) ?=> A =
        effect ?=> effect.format.underline.none(() => f(using effect))

      def straight[F, A](
          f: F ?=> A
      ): (F & effect.Format) ?=> A =
        effect ?=> effect.format.underline.straight(() => f(using effect))

      def double[F, A](
          f: F ?=> A
      ): (F & effect.Format) ?=> A =
        effect ?=> effect.format.underline.double(() => f(using effect))

      def curly[F, A](
          f: F ?=> A
      ): (F & effect.Format) ?=> A =
        effect ?=> effect.format.underline.curly(() => f(using effect))

      def dotted[F, A](
          f: F ?=> A
      ): (F & effect.Format) ?=> A =
        effect ?=> effect.format.underline.dotted(() => f(using effect))

      def dashed[F, A](
          f: F ?=> A
      ): (F & effect.Format) ?=> A =
        effect ?=> effect.format.underline.dashed(() => f(using effect))

      def default[F, A](
          f: F ?=> A
      ): (F & effect.Format) ?=> A =
        effect ?=> effect.format.underline.default(() => f(using effect))

      def black[F, A](
          f: F ?=> A
      ): (F & effect.Format) ?=> A =
        effect ?=> effect.format.underline.black(() => f(using effect))

      def red[F, A](
          f: F ?=> A
      ): (F & effect.Format) ?=> A =
        effect ?=> effect.format.underline.red(() => f(using effect))

      def green[F, A](
          f: F ?=> A
      ): (F & effect.Format) ?=> A =
        effect ?=> effect.format.underline.green(() => f(using effect))

      def yellow[F, A](
          f: F ?=> A
      ): (F & effect.Format) ?=> A =
        effect ?=> effect.format.underline.yellow(() => f(using effect))

      def blue[F, A](
          f: F ?=> A
      ): (F & effect.Format) ?=> A =
        effect ?=> effect.format.underline.blue(() => f(using effect))

      def magenta[F, A](
          f: F ?=> A
      ): (F & effect.Format) ?=> A =
        effect ?=> effect.format.underline.magenta(() => f(using effect))

      def cyan[F, A](
          f: F ?=> A
      ): (F & effect.Format) ?=> A =
        effect ?=> effect.format.underline.cyan(() => f(using effect))

      def white[F, A](
          f: F ?=> A
      ): (F & effect.Format) ?=> A =
        effect ?=> effect.format.underline.white(() => f(using effect))

    def blink[F, A](
        f: F ?=> A
    ): (F & effect.Format) ?=> A =
      effect ?=> effect.format.blink(() => f(using effect))

    def invert[F, A](
        f: F ?=> A
    ): (F & effect.Format) ?=> A =
      effect ?=> effect.format.invert(() => f(using effect))

    def invisible[F, A](
        f: F ?=> A
    ): (F & effect.Format) ?=> A =
      effect ?=> effect.format.invisible(() => f(using effect))

    def strikethrough[F, A](
        f: F ?=> A
    ): (F & effect.Format) ?=> A =
      effect ?=> effect.format.strikethrough(() => f(using effect))
