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

import org.jline.terminal.Size
import org.jline.terminal.Terminal as JTerminal
import org.jline.terminal.TerminalBuilder
import org.jline.utils.InfoCmp.Capability
import terminus.effect.Eof
import terminus.effect.TerminalDimensions

class JLineTerminal(terminal: JTerminal) extends Terminal {
  private val reader = terminal.reader()
  private val writer = terminal.writer()

  def read(): Eof | Char =
    reader.read() match {
      case -1   => Eof
      case char => char.toChar
    }

  def flush(): Unit = writer.flush()

  def write(char: Char): Unit = writer.write(char)

  def write(string: String): Unit = writer.write(string)

  def raw[A](f: Terminal ?=> A): A = {
    val attrs = terminal.enterRawMode()
    try {
      val result = f(using this)
      result
    } finally {
      terminal.setAttributes(attrs)
    }
  }

  def getDimensions: effect.TerminalDimensions = {
    val size = terminal.getSize
    TerminalDimensions(size.getColumns, size.getRows)
  }

  def setDimensions(dimensions: TerminalDimensions): Unit =
    terminal.setSize(Size(dimensions.noOfColumns, dimensions.noOfRows))

  def application[A](f: Terminal ?=> A): A = {
    try {
      terminal.puts(Capability.keypad_xmit)
      val result = f(using this)
      result
    } finally {
      val _ = terminal.puts(Capability.keypad_local)
    }
  }

  def alternateScreen[A](f: Terminal ?=> A): A = {
    try {
      terminal.puts(Capability.enter_ca_mode)
      val result = f(using this)
      result
    } finally {
      val _ = terminal.puts(Capability.exit_ca_mode)
    }
  }

  def close(): Unit = terminal.close()
}
object JLineTerminal
    extends Color,
      Cursor,
      Format,
      Dimensions,
      Erase,
      AlternateScreenMode,
      ApplicationMode,
      RawMode,
      Reader,
      Writer {
  def apply: JLineTerminal = new JLineTerminal(
    TerminalBuilder.builder().build()
  )

  def run[A](f: Program[A]): A = {
    val terminal = Terminal.apply
    val result = f(using terminal)

    terminal.close()
    result
  }
}
