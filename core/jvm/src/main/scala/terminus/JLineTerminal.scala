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

import org.jline.terminal.Terminal as JTerminal
import terminus.effect.Eof

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
    val result = f(using this)

    terminal.setAttributes(attrs)
    result
  }

  def close(): Unit = terminal.close()
}
