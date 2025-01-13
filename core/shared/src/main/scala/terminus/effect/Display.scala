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

/** Terminal effects that can change display properties. */
trait Display[+F <: Writer] extends WithStack[F], WithToggle[F] { self: F =>
  object display {
    // Bold and light share this stack
    private val fontWeightStack =
      Stack(AnsiCodes.display.bold.off)

    def bold[A](f: F ?=> A): A =
      withStack(fontWeightStack, AnsiCodes.display.bold.on)(f)

    def light[A](f: F ?=> A): A =
      withStack(fontWeightStack, AnsiCodes.display.light.on)(f)

    /** Normal weight text, neither bold nor light. */
    def normal[A](f: F ?=> A): A =
      withStack(fontWeightStack, AnsiCodes.display.bold.off)(f)

    object underline {
      private val underlineStyleStack = Stack(AnsiCodes.display.underline.off)

      def none[A](f: F ?=> A): A =
        withStack(underlineStyleStack, AnsiCodes.display.underline.off)(f)

      def straight[A](f: F ?=> A): A =
        withStack(underlineStyleStack, AnsiCodes.display.underline.straight)(f)

      def double[A](f: F ?=> A): A =
        withStack(underlineStyleStack, AnsiCodes.display.underline.double)(f)

      def curly[A](f: F ?=> A): A =
        withStack(underlineStyleStack, AnsiCodes.display.underline.curly)(f)

      def dotted[A](f: F ?=> A): A =
        withStack(underlineStyleStack, AnsiCodes.display.underline.dotted)(f)

      def dashed[A](f: F ?=> A): A =
        withStack(underlineStyleStack, AnsiCodes.display.underline.dashed)(f)

      private val underlineColorStack = Stack(
        AnsiCodes.display.underline.default
      )

      def default[A](f: F ?=> A): A =
        withStack(underlineColorStack, AnsiCodes.display.underline.default)(f)

      def black[A](f: F ?=> A): A =
        withStack(underlineColorStack, AnsiCodes.display.underline.black)(f)

      def red[A](f: F ?=> A): A =
        withStack(underlineColorStack, AnsiCodes.display.underline.red)(f)

      def green[A](f: F ?=> A): A =
        withStack(underlineColorStack, AnsiCodes.display.underline.green)(f)

      def yellow[A](f: F ?=> A): A =
        withStack(underlineColorStack, AnsiCodes.display.underline.yellow)(f)

      def blue[A](f: F ?=> A): A =
        withStack(underlineColorStack, AnsiCodes.display.underline.blue)(f)

      def magenta[A](f: F ?=> A): A =
        withStack(underlineColorStack, AnsiCodes.display.underline.magenta)(f)

      def cyan[A](f: F ?=> A): A =
        withStack(underlineColorStack, AnsiCodes.display.underline.cyan)(f)

      def white[A](f: F ?=> A): A =
        withStack(underlineColorStack, AnsiCodes.display.underline.white)(f)
    }

    private val blinkToggle =
      Toggle(AnsiCodes.display.blink.on, AnsiCodes.display.blink.off)

    def blink[A](f: F ?=> A): A =
      withToggle(blinkToggle)(f)

    private val invertToggle =
      Toggle(AnsiCodes.display.invert.on, AnsiCodes.display.invert.off)

    def invert[A](f: F ?=> A): A =
      withToggle(invertToggle)(f)

    private val invisibleToggle =
      Toggle(AnsiCodes.display.invisible.on, AnsiCodes.display.invisible.off)

    def invisible[A](f: F ?=> A): A =
      withToggle(invisibleToggle)(f)

    private val strikethroughToggle = Toggle(
      AnsiCodes.display.strikethrough.on,
      AnsiCodes.display.strikethrough.off
    )

    def strikethrough[A](f: F ?=> A): A =
      withToggle(strikethroughToggle)(f)
  }
}
