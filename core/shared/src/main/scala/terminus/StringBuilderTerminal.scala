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

import scala.collection.mutable

final class StringBuilderTerminal()
    extends effect.Color[StringBuilderTerminal],
      effect.Cursor,
      effect.Display[StringBuilderTerminal],
      effect.Erase,
      effect.Writer {

  private val builder = mutable.StringBuilder()

  /** Flush is a no-op */
  def flush(): Unit = ()

  def write(char: Char): Unit =
    builder.addOne(char)

  def write(string: String): Unit =
    builder.addAll(string)

  /** Get the value accumulated in the internal string builder, clearing the
    * buffer in the process.
    */
  def result(): String = {
    val s = builder.result()
    builder.clear()
    s
  }
}
object StringBuilderTerminal {
  type Program[A] = StringBuilderTerminal ?=> A

  def run[A](f: Program[A]): String = {
    val terminal = StringBuilderTerminal()
    val _ = f(using terminal)

    terminal.result()
  }
}
