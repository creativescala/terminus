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

import scalanative.unsafe.*
import scalanative.unsafe.Nat.*
import scalanative.posix

/** The Scala Native implementation of termios.h assumes certain types are
  * implemented as CLong. This is correct on macOS, but not on Linux. This
  * alternative implementation works on Linux.
  */
object LinuxTermios extends Termios {
  private val STDIN = scala.scalanative.posix.unistd.STDIN_FILENO

  type Attributes = Ptr[termios]

  // Copied from the Scala Native implementation with appropriate changes for Linux
  //
  // Original is at
  // https://github.com/scala-native/scala-native/blob/main/posixlib/src/main/scala/scala/scalanative/posix/termios.scala
  //
  // See
  // https://github.com/lattera/glibc/blob/master/bits/termios.h

  // types

  type tcflag_t = CInt // Changed
  type cc_t = CChar
  type speed_t = CInt // Changed
  type NCCS = Digit2[_2, _0]
  type c_cc = CArray[cc_t, NCCS]

  type termios = CStruct7[
    tcflag_t, /* c_iflag - input flags   */
    tcflag_t, /* c_oflag - output flags  */
    tcflag_t, /* c_cflag - control flags */
    tcflag_t, /* c_lflag - local flags   */
    c_cc, /* cc_t c_cc[NCCS] - control chars */
    speed_t, /* c_ispeed - input speed   */
    speed_t /* c_ospeed - output speed  */
  ]

  // functions

  def setVMin(attr: Attributes, byte: CChar): Unit =
    attr._5(posix.termios.VMIN) = byte

  def setVTime(attr: Attributes, byte: CChar): Unit =
    attr._5(posix.termios.VTIME) = byte

  @extern
  def tcgetattr(fd: CInt, termios_p: Ptr[termios]): CInt = extern
  @extern
  def tcsetattr(
      fd: CInt,
      optionalActions: CInt,
      termios_p: Ptr[termios]
  ): CInt = extern

  def getAttributes()(using Zone): Attributes = {
    val attrs: Attributes = alloc[termios]()
    tcgetattr(STDIN, attrs)
    attrs
  }

  def setAttributes(attributes: Attributes): Unit = {
    val _ = tcsetattr(STDIN, posix.termios.TCSANOW, attributes)
    ()
  }

  def setRawMode(): Unit = {
    Zone {
      val attrs = getAttributes()
      attrs._1 = attrs._1 & ~(posix.termios.ICRNL)
      attrs._4 = attrs._4 & ~(posix.termios.ECHO | posix.termios.ICANON)
      setAttributes(attrs)
    }
  }
}
