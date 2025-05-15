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
import terminus.effect.TerminalDimensions
import terminus.effect.TerminalKeyReader

import scala.concurrent.duration.Duration

class JLineTerminal(terminal: JTerminal) extends Terminal, TerminalKeyReader {
  private val reader = terminal.reader()
  private val writer = terminal.writer()

  def peek(duration: Duration): Timeout | Eof | Char =
    reader.peek(duration.toMillis) match {
      case -2   => Timeout
      case -1   => Eof
      case char => char.toChar
    }

  def read(duration: Duration): Timeout | Eof | Char =
    reader.read(duration.toMillis) match {
      case -2   => Timeout
      case -1   => Eof
      case char => char.toChar
    }

  def read(): Eof | Char =
    reader.read() match {
      case -1   => Eof
      case char => char.toChar
    }

  def flush(): Unit = writer.flush()

  def write(char: Char): Unit = writer.write(char)

  def write(string: String): Unit = writer.write(string)

  private[terminus] def setRawMode(): () => Unit = {
    val attrs = terminal.enterRawMode()
    () => terminal.setAttributes(attrs)
  }

  def getDimensions: effect.TerminalDimensions = {
    val size = terminal.getSize
    TerminalDimensions(size.getColumns, size.getRows)
  }

  def setDimensions(dimensions: TerminalDimensions): Unit =
    terminal.setSize(Size(dimensions.columns, dimensions.rows))

  private[terminus] def setApplicationMode(): () => Unit = {
    terminal.puts(Capability.keypad_xmit)
    () => {
      terminal.puts(Capability.keypad_local)
      ()
    }
  }

  private[terminus] def setAlternateScreenMode(): () => Unit = {
    terminal.puts(Capability.enter_ca_mode)
    () => {
      terminal.puts(Capability.exit_ca_mode)
      ()
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
