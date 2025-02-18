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

import scala.annotation.implicitNotFound
import scala.scalanative.posix
import scala.scalanative.posix.unistd.STDIN_FILENO
import scala.scalanative.unsafe.*

/** Typeclass for handling the messiness of reading, updating, and writing
  * termios structs in a platform specific way.
  *
  * Unfortunately, while all POSIX operating systems have `termios.h` defined,
  * there `termios` structure is inconsistent in the size of the bitflags used
  * to hold the terminal settings. As an example, Linux uses CInt to hold
  * bitflags while OSX uses CLong.
  *
  * At the time of this writing, the Scala Native implementation does not
  * currently handle OS specific differences in
  *
  * @see
  *   [[https://github.com/scala-native/scala-native/issues/4143 Scala Native termios linux issue]]
  *
  * @tparam T
  *   The type of the termios structure, should be on of either
  *   [[TermiosStruct.cint_flags]] or [[TermiosStruct.clong_flags]]. At the time
  *   of writing this documentation, these are the only known variants of the
  *   termios struct. If more are found in the future, they should be added.
  */
@implicitNotFound(
  "There is no terminus.TermiosAccess instance defined for the given termios struct."
)
trait TermiosAccess[T] {

  /** The flag value used by the termios struct the typeclass is defined for, is
    * either a CInt or CLong
    */
  type FlagType <: CInt | CLong

  /** Allocates a new termios structure defined by [[this.T]], in the given
    * [[Zone]] and copies the current terminal settings into it, returning a
    * pointer in memory to this structure.
    */
  def get(using Zone): Ptr[T]

  /** Sets the terminal settings to the attributes defined by the termios
    * structure stored at the pointer specified by the [[ptr]] argument.
    */
  def set(ptr: Ptr[T]): Unit

  // Methods for updating values in a termios struct. Methods for manipulating control characters
  // and speed settings can be added if necessary.

  /** Add flags to termios c_iflag struct member */
  def addInputFlags(attrs: Ptr[T], flags: CInt): Unit

  /** Remove flags from termios c_iflag struct member */
  def removeInputFlags(attrs: Ptr[T], flags: CInt): Unit

  /** Add flags to termios c_oflag struct member */
  def addOutputFlags(attrs: Ptr[T], flags: CInt): Unit

  /** Remove flags from termios c_oflag struct member */
  def removeOutputFlags(attrs: Ptr[T], flags: CInt): Unit

  /** Add flags to termios c_cflag struct member */
  def addControlFlags(attrs: Ptr[T], flags: CInt): Unit

  /** Remove flags from termios c_cflag struct member */
  def removeControlFlags(attrs: Ptr[T], flags: CInt): Unit

  /** Add flags to termios c_lflag struct member */
  def addLocalFlags(attrs: Ptr[T], flags: CInt): Unit

  /** Remove flags from termios c_lflag struct member */
  def removeLocalFlags(attrs: Ptr[T], flags: CInt): Unit
}

/** Extension methods to simplify modifying a termios struct */
extension [T](ptr: Ptr[T])(using au: TermiosAccess[T]) {
  def addInputFlags(flags: CInt): Unit = au.addInputFlags(ptr, flags)
  def removeInputFlags(flags: CInt): Unit = au.removeInputFlags(ptr, flags)
  def addOutputFlags(flags: CInt): Unit = au.addOutputFlags(ptr, flags)
  def removeOutputFlags(flags: CInt): Unit = au.removeOutputFlags(ptr, flags)
  def addControlFlags(flags: CInt): Unit = au.addControlFlags(ptr, flags)
  def removeControlFlags(flags: CInt): Unit = au.removeControlFlags(ptr, flags)
  def addLocalFlags(flags: CInt): Unit = au.addLocalFlags(ptr, flags)
  def removeLocalFlags(flags: CInt): Unit = au.removeLocalFlags(ptr, flags)
}

/** [[TermiosAccess]] instance for structs with CLong bitflags */
given clongTermiosAccess: TermiosAccess[TermiosStruct.clong_flags] =
  new TermiosAccess[TermiosStruct.clong_flags] {
    override type FlagType = CLong

    override def get(using Zone): Ptr[TermiosStruct.clong_flags] = {
      val attrs: Ptr[TermiosStruct.clong_flags] =
        alloc[TermiosStruct.clong_flags]()
      posix.termios.tcgetattr(STDIN_FILENO, attrs)
      attrs
    }

    override def set(ptr: Ptr[TermiosStruct.clong_flags]): Unit = {
      val _ =
        posix.termios.tcsetattr(STDIN_FILENO, posix.termios.TCSAFLUSH, ptr)
      ()
    }

    override def addInputFlags(
        attrs: Ptr[TermiosStruct.clong_flags],
        flags: CInt
    ): Unit =
      attrs._1 = attrs._1 | flags

    override def removeInputFlags(
        attrs: Ptr[TermiosStruct.clong_flags],
        flags: CInt
    ): Unit =
      attrs._1 = attrs._1 & ~flags

    override def addOutputFlags(
        attrs: Ptr[TermiosStruct.clong_flags],
        flags: CInt
    ): Unit =
      attrs._2 = attrs._2 | flags

    override def removeOutputFlags(
        attrs: Ptr[TermiosStruct.clong_flags],
        flags: CInt
    ): Unit =
      attrs._2 = attrs._2 & ~flags

    override def addControlFlags(
        attrs: Ptr[TermiosStruct.clong_flags],
        flags: CInt
    ): Unit =
      attrs._3 = attrs._3 | flags

    override def removeControlFlags(
        attrs: Ptr[TermiosStruct.clong_flags],
        flags: CInt
    ): Unit =
      attrs._3 = attrs._3 & ~flags

    override def addLocalFlags(
        attrs: Ptr[TermiosStruct.clong_flags],
        flags: CInt
    ): Unit =
      attrs._4 = attrs._4 | flags

    override def removeLocalFlags(
        attrs: Ptr[TermiosStruct.clong_flags],
        flags: CInt
    ): Unit =
      attrs._4 = attrs._4 & ~flags
  }

/** [[TermiosAccess]] instance for structs with CInt bitflags */
given cintTermiosAccess: TermiosAccess[TermiosStruct.cint_flags] =
  new TermiosAccess[TermiosStruct.cint_flags] {
    override type FlagType = CInt

    override def get(using Zone): Ptr[TermiosStruct.cint_flags] = {
      val attrs: Ptr[TermiosStruct.cint_flags] =
        alloc[TermiosStruct.cint_flags]()
      tcgetattr(STDIN_FILENO, attrs)
      attrs
    }

    override def set(ptr: Ptr[TermiosStruct.cint_flags]): Unit = {
      val _ = tcsetattr(STDIN_FILENO, posix.termios.TCSAFLUSH, ptr)
      ()
    }

    override def addInputFlags(
        attrs: Ptr[TermiosStruct.cint_flags],
        flags: CInt
    ): Unit =
      attrs._1 = attrs._1 | flags

    override def removeInputFlags(
        attrs: Ptr[TermiosStruct.cint_flags],
        flags: CInt
    ): Unit =
      attrs._1 = attrs._1 & ~flags

    override def addOutputFlags(
        attrs: Ptr[TermiosStruct.cint_flags],
        flags: CInt
    ): Unit =
      attrs._2 = attrs._2 | flags

    override def removeOutputFlags(
        attrs: Ptr[TermiosStruct.cint_flags],
        flags: CInt
    ): Unit =
      attrs._2 = attrs._2 & ~flags

    override def addControlFlags(
        attrs: Ptr[TermiosStruct.cint_flags],
        flags: CInt
    ): Unit =
      attrs._3 = attrs._3 | flags

    override def removeControlFlags(
        attrs: Ptr[TermiosStruct.cint_flags],
        flags: CInt
    ): Unit =
      attrs._3 = attrs._3 & ~flags

    override def addLocalFlags(
        attrs: Ptr[TermiosStruct.cint_flags],
        flags: CInt
    ): Unit = attrs._4 = attrs._4 | flags

    override def removeLocalFlags(
        attrs: Ptr[TermiosStruct.cint_flags],
        flags: CInt
    ): Unit = attrs._4 = attrs._4 & ~flags

    // Custom `tcgetattr` and `tcsetattr` definitions, since we can't use the ones defined in scala native since
    // they use CLong for bitflags. These should point to the functions defined in the systems termios library

    @extern
    def tcgetattr(fd: CInt, termios_p: Ptr[TermiosStruct.cint_flags]): CInt =
      extern

    @extern
    def tcsetattr(
        fd: CInt,
        optionalActions: CInt,
        termios_p: Ptr[TermiosStruct.cint_flags]
    ): CInt = extern
  }
