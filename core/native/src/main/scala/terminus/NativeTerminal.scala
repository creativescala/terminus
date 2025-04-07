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
import terminus.effect.TerminalKeyReader
import terminus.effect.WithEffect

import scala.concurrent.duration.Duration
import scala.scalanative.libc
import scala.scalanative.meta.LinktimeInfo
import scala.scalanative.posix
import scala.scalanative.unsigned.UInt

import scalanative.unsafe.*

/** A Terminal implementation for Scala Native. */
object NativeTerminal
    extends Terminal,
      WithEffect[Terminal],
      TerminalKeyReader {

  private given termiosAccess: TermiosAccess[?] =
    if LinktimeInfo.isMac then clongTermiosAccess
    else if LinktimeInfo.isLinux then cintTermiosAccess
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

  private val termios = Termios(using termiosAccess)

  def run[A](f: Program[A]): A = {
    val result = f(using this)

    result
  }

  def read(): Eof | Char = {
    val buf: Ptr[Byte] = stackalloc[Byte]()
    val count =
      posix.unistd.read(posix.unistd.STDIN_FILENO, buf, UInt.valueOf(1))
    if count == 0 then Eof
    else (!buf).toChar
  }

  def read(duration: Duration): Timeout | Eof | Char = {
    Zone {
      val origAttrs = termios.getAttributes()
      val attrs = termios.getAttributes()
      try {
        attrs.setSpecialCharacter(posix.termios.VMIN, 0)
        attrs.setSpecialCharacter(
          posix.termios.VTIME,
          (duration.toMillis / 100).toByte
        )
        termios.setAttributes(attrs)

        val buf: Ptr[Byte] = stackalloc[Byte]()
        val count =
          posix.unistd.read(posix.unistd.STDIN_FILENO, buf, UInt.valueOf(1))

        if count == 0 then Timeout
        else if count == -1 then Eof
        else {
          (!buf).toChar
        }

      } finally {
        termios.setAttributes(origAttrs)
      }
    }
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

  private[terminus] def setRawMode(): () => Unit = {
    implicit val z: Zone = Zone.open()
    val origAttrs = termios.getAttributes()
    termios.setRawMode()

    () => {
      termios.setAttributes(origAttrs)
      z.close()
    }
  }

  private[terminus] def setApplicationMode(): () => Unit =
    effectDeferRollback(
      AnsiCodes.mode.application.on,
      AnsiCodes.mode.application.off
    )

  private[terminus] def setAlternateScreenMode(): () => Unit =
    effectDeferRollback(
      AnsiCodes.mode.alternateScreen.on,
      AnsiCodes.mode.alternateScreen.off
    )
}
