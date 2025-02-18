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

import scala.scalanative.posix
import scala.scalanative.unsafe.*

trait TermiosAccess[T] {
  val STDIN = scala.scalanative.posix.unistd.STDIN_FILENO

  type FlagType <: CInt | CLong

  def get(using Zone): Ptr[T]
  def set(ptr: Ptr[T]): Unit

  def addLocalFlags(attrs: Ptr[T], flags: CInt): Unit
  def removeLocalFlags(attrs: Ptr[T], flags: CInt): Unit
}

extension [T](ptr: Ptr[T])(using au: TermiosAccess[T]) {
  def addLocalFlags(flags: CInt): Unit = au.addLocalFlags(ptr, flags)
  def removeLocalFlags(flags: CInt): Unit = au.removeLocalFlags(ptr, flags)
}

given clongTermiosAccess: TermiosAccess[TermiosStruct.clong_flags] =
  new TermiosAccess[TermiosStruct.clong_flags] {
    override type FlagType = CLong

    override def get(using Zone): Ptr[TermiosStruct.clong_flags] = {
      val attrs: Ptr[TermiosStruct.clong_flags] = alloc[TermiosStruct.clong_flags]()
      posix.termios.tcgetattr(STDIN, attrs)
      attrs
    }

    override def set(ptr: Ptr[TermiosStruct.clong_flags]): Unit = {
      val _ = posix.termios.tcsetattr(STDIN, posix.termios.TCSAFLUSH, ptr)
      ()
    }

    def addLocalFlags(attrs: Ptr[TermiosStruct.clong_flags], flags: CInt): Unit =
      attrs._4 = attrs._4 | flags

    def removeLocalFlags(attrs: Ptr[TermiosStruct.clong_flags], flags: CInt): Unit =
      attrs._4 = attrs._4 & ~flags
  }

given cintTermiosAccess: TermiosAccess[TermiosStruct.cint_flags] =
  new TermiosAccess[TermiosStruct.cint_flags] {
    override type FlagType = CInt

    override def get(using Zone): Ptr[TermiosStruct.cint_flags] = {
      val attrs: Ptr[TermiosStruct.cint_flags] = alloc[TermiosStruct.cint_flags]()
      tcgetattr(STDIN, attrs)
      attrs
    }

    override def set(ptr: Ptr[TermiosStruct.cint_flags]): Unit = {
      val _ = tcsetattr(STDIN, posix.termios.TCSAFLUSH, ptr)
      ()
    }

    override def addLocalFlags(
                                attrs: Ptr[TermiosStruct.cint_flags],
                                flags: CInt
    ): Unit = attrs._4 = attrs._4 | flags

    override def removeLocalFlags(
                                   attrs: Ptr[TermiosStruct.cint_flags],
                                   flags: CInt
    ): Unit = attrs._4 = attrs._4 & ~flags

    @extern
    def tcgetattr(fd: CInt, termios_p: Ptr[TermiosStruct.cint_flags]): CInt = extern

    @extern
    def tcsetattr(
        fd: CInt,
        optionalActions: CInt,
        termios_p: Ptr[TermiosStruct.cint_flags]
    ): CInt = extern
  }
