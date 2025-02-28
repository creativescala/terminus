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
import scala.scalanative.unsafe.Ptr
import scala.scalanative.unsafe.Zone

/** An abstraction over the c termios library that contains only the
  * functionality that we need, hiding the details of reading, updating, and
  * writing terminal settings from the caller. The details are handled by a
  * [[TermiosAccess]] typeclass instance.
  *
  * @see
  *   [[TermiosAccess]]
  * @see
  *   [[TermiosStruct]]
  *
  * @param accessor
  *   An instance of [[TermiosAccess]] used to handle the actual reading,
  *   updating, and writing of terminal settings.
  * @tparam T
  *   The termios structure type a Termios instance uses. See [[TermiosStruct]]
  *   for more details
  */
class Termios[T](using accessor: TermiosAccess[T]) {

  /** Allocates a new termios structure defined by [[this.T]], in the given
    * [[Zone]] and copies the current terminal settings into it, returning a
    * pointer in memory to this structure.
    */
  def getAttributes()(using Zone): Ptr[T] = accessor.get

  /** Sets the terminal settings to the attributes defined by the termios
    * structure stored at the pointer specified by the [[attributes]] argument.
    */
  def setAttributes(attributes: Ptr[T]): Unit = accessor.set(attributes)

  /** Places the terminal in raw mode.
    *
    * @see
    *   [[https://en.wikipedia.org/wiki/Terminal_mode Terminal Modes]]
    */
  def setRawMode(): Unit =
    Zone {
      val attrs = accessor.get
      attrs.removeLocalFlags(posix.termios.ECHO | posix.termios.ICANON)
      accessor.set(attrs)
    }
}
