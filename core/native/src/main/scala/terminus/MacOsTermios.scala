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
import scalanative.posix

/** macOS implementation of Termios, which just uses the Scala Native
  * implementation
  */
object MacOsTermios extends Termios {
  // https://viewsourcecode.org/snaptoken/kilo/index.html is a good introduction
  // to using the C API to control the terminal.
  type Attributes = Ptr[posix.termios.termios]

  private val STDIN = scala.scalanative.posix.unistd.STDIN_FILENO

  def getAttributes()(using Zone): Attributes = {
    val attrs: Attributes = alloc[posix.termios.termios]()
    posix.termios.tcgetattr(STDIN, attrs)
    attrs
  }

  def setRawMode(): Unit = {
    Zone {
      val attrs = getAttributes()
      attrs._4 = attrs._4 & ~(posix.termios.ECHO | posix.termios.ICANON)
      setAttributes(attrs)
    }
  }

  def setAttributes(attributes: Attributes): Unit = {
    val _ = posix.termios.tcsetattr(STDIN, posix.termios.TCSAFLUSH, attributes)
    ()
  }
}
