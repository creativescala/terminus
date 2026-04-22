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

import munit.FunSuite

import scala.scalanative.meta.LinktimeInfo
import scala.scalanative.posix
import scala.scalanative.unsafe.CInt
import scala.scalanative.unsafe.Ptr
import scala.scalanative.unsafe.Zone
import scala.scalanative.unsigned.*

class TermiosAccessSuite extends FunSuite:
  private given termios: TermiosAccess[?] =
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

  def testSettingSpecialCharacter[T](
      attrs: Ptr[T],
      idx: CInt,
      value: UByte,
      name: String
  )(using termios: TermiosAccess[T], zone: Zone): Unit =
    attrs.setSpecialCharacter(idx, value)
    termios.set(attrs)
    val updated = termios.get
    val read = updated.getSpecialCharacter(idx)
    assertEquals(read, value, name)

  test("Test reading special characters returns set value") {
    Zone {
      val orig = termios.get
      try
        val attrs = termios.get

        List(
          (posix.termios.VEOF, "VEOF", Seq(0)),
          (posix.termios.VEOL, "VEOL", Seq(1, 2)),
          (posix.termios.VERASE, "VERASE", Seq(1, 2, 3, 4)),
          (posix.termios.VINTR, "VINTR", Seq(1, 2, 3, 4)),
          (posix.termios.VKILL, "VKILL", Seq(1, 2, 3, 4)),
          (posix.termios.VMIN, "VMIN", Seq(0, 1, 2)),
          (posix.termios.VQUIT, "VQUIT", Seq(1, 2, 3, 4)),
          (posix.termios.VSTART, "VSTART", Seq(1, 2, 3, 4)),
          (posix.termios.VSTOP, "VSTOP", Seq(1, 2, 3, 4)),
          (posix.termios.VSUSP, "VSUSP", Seq(1, 2, 3, 4)),
          (posix.termios.VTIME, "VTIME", Seq(1, 2, 3, 4))
        ).foreach { (idx, name, values) =>
          values.foreach { v =>
            testSettingSpecialCharacter(attrs, idx, v.toUByte, name)
          }
        }
      finally termios.set(orig)
    }
  }
