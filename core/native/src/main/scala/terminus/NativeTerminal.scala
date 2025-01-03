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

import terminus.effect.Eof

import scala.scalanative.libc
import scala.scalanative.posix.termios

import scalanative.unsafe.*

/** A Terminal implementation for Scala Native. */
object NativeTerminal extends Terminal {
  // https://viewsourcecode.org/snaptoken/kilo/index.html is a good introduction
  // to using the C API to control the terminal.

  private val STDIN = scala.scalanative.posix.unistd.STDIN_FILENO

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
    // See https://www.vt100.net/docs/vt510-rm/DECCKM
    csi("?1h")
    val result = f(using this)
    csi("?1l")

    result
  }

  def alternateScreen[A](f: (terminus.Terminal) ?=> A): A = {
    csi("?1049h")
    val result = f(using this)
    csi("?1049l")

    result
  }

  def raw[A](f: Terminal ?=> A): A = {
    val origAttrs: Ptr[termios.termios] = stackalloc[termios.termios]()
    val flags = termios.TCSAFLUSH

    termios.tcgetattr(STDIN, origAttrs)

    try {
      val rawAttrs = stackalloc[termios.termios]()
      termios.tcgetattr(STDIN, rawAttrs)
      // _4 is c_lflag
      rawAttrs._4 = rawAttrs._4 & ~(termios.ECHO | termios.ICANON)
      termios.tcsetattr(STDIN, flags, rawAttrs)

      f(using this)
    } finally {
      val _ = termios.tcsetattr(STDIN, flags, origAttrs)
    }
  }
}
