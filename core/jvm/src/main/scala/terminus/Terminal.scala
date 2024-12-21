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

import org.jline.terminal.TerminalBuilder

trait Terminal extends effect.Reader, effect.Writer, effect.Color[Terminal], effect.Mode[Terminal]
type Program[A] = Terminal ?=> A

object Terminal extends Reader, Writer, Color, Mode {
  def apply: JLineTerminal = JLineTerminal(TerminalBuilder.builder().build())

  def run[A](f: Program[A]): A = {
    val terminal = Terminal.apply
    val result = f(using terminal)

    terminal.close()
    result
  }
}
