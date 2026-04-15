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

import scala.scalanative.posix
import scala.scalanative.unsafe.CInt
import scala.scalanative.unsafe.Ptr
import scala.scalanative.unsafe.Zone
import scala.scalanative.unsigned.*

class CLongTermiosAccessSuite extends FunSuite {
  val termios = clongTermiosAccess

  def testSettingSpecialCharacter(
      attrs: Ptr[TermiosStruct.clong_flags],
      idx: CInt,
      value: UByte,
      name: String
  )(using Zone): Unit = {
    attrs.setSpecialCharacter(idx, value)
    termios.set(attrs)
    val updated = termios.get
    val read = updated.getSpecialCharacter(idx)
    assertEquals(read, value, name)
  }

  test("Test reading special characters returns set value") {
    Zone {
      val orig = termios.get
      try {
        val attrs = termios.get

        List(
          (posix.termios.VEOF, "VEOF"),
          (posix.termios.VEOL, "VEOL"),
          (posix.termios.VERASE, "VERASE"),
          (posix.termios.VINTR, "VINTR"),
          (posix.termios.VKILL, "VKILL"),
          (posix.termios.VMIN, "VMIN"),
          (posix.termios.VQUIT, "VQUIT"),
          (posix.termios.VSTART, "VSTART"),
          (posix.termios.VSTOP, "VSTOP"),
          (posix.termios.VSUSP, "VSUSP"),
          (posix.termios.VTIME, "VTIME")
        ).foreach { (idx, name) =>
          testSettingSpecialCharacter(attrs, idx, 1.toUByte, name)
          testSettingSpecialCharacter(attrs, idx, 2.toUByte, name)
          testSettingSpecialCharacter(attrs, idx, 3.toUByte, name)
          testSettingSpecialCharacter(attrs, idx, 4.toUByte, name)
        }
      } finally {
        termios.set(orig)
      }
    }
  }
}
