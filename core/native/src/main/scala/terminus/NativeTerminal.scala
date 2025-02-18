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

import terminus.effect.AnsiCodes
import terminus.effect.Eof
import terminus.effect.WithEffect

import scala.scalanative.libc
import scala.scalanative.meta.LinktimeInfo

import scalanative.unsafe.*

/** A Terminal implementation for Scala Native. */
object NativeTerminal extends Terminal, WithEffect[Terminal] {
  val termios =
    if LinktimeInfo.isMac then Termios[TermiosTypes.mac]
    else if LinktimeInfo.isLinux then Termios[TermiosTypes.linux]
    else
      sys.error(
        s"""Your platform, {LinktimeInfo.target.os}, is not currently supported by Terminus on Scala Native.
           |
           |You can use a different backend, such as the JVM, which is likely to support your platform. You can
           |also open an issue at
           |
           |   https://github.com/creativescala/terminus/issues
           |
           |to get support added for your platform.""".stripMargin
      )

  def run[A](f: Program[A]): A = {
    val result = f(using this)

    result
  }

  def read(): Eof | Char = {
    val char = libc.stdio.fgetc(libc.stdio.stdin)
    if char == libc.stdio.EOF then Eof
    else char.toChar
  }

  def flush(): Unit = {
    val _ = libc.stdio.fflush(libc.stdio.stdin)
    ()
  }

  def write(char: Char): Unit = {
    val _ = libc.stdio.fputc(char, libc.stdio.stdout)
    ()
  }

  def write(string: String): Unit = {
    // print(string)
    Zone {
      val _ = libc.stdio.fputs(toCString(string), libc.stdio.stdout)
      ()
    }
  }

  def application[A](f: (terminus.Terminal) ?=> A): A = {
    withEffect(AnsiCodes.mode.application.on, AnsiCodes.mode.application.off)(f)
  }

  def alternateScreen[A](f: (terminus.Terminal) ?=> A): A = {
    withEffect(
      AnsiCodes.mode.alternateScreen.on,
      AnsiCodes.mode.alternateScreen.off
    )(f)
  }

  def raw[A](f: Terminal ?=> A): A = {
    Zone {
      val origAttrs = termios.getAttributes()
      try {
        termios.setRawMode()
        f(using this)
      } finally {
        termios.setAttributes(origAttrs)
      }
    }
  }
}
