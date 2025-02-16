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

package terminus.effect

import scala.collection.mutable

/** A `Stack` handles stack for Terminal effects that overwrite each other
  * within nested blocks. The canoncial example is color. There can only be one
  * foreground or background color in use at any one time, but the color revert
  * to that of the surrounding block when a given block exits. This is handles
  * by storing a stack of color codes, and doing the appropriate actions on
  * entering a block (a `push`) and exiting a block (a `pop`).
  *
  * @param `reset`:
  *   The escape code to emit when there are no elements left on the stack.
  */
final case class Stack(reset: String) {
  private val stack: mutable.Stack[String] = mutable.Stack()

  def push(code: String, writer: Writer): Unit =
    stack.headOption match {
      case None =>
        stack.push(code)
        writer.write(code)

      case Some(value) =>
        if value == code then {
          stack.push(code)
        } else {
          stack.push(code)
          writer.write(code)
        }
    }

  def pop(writer: Writer): Unit = {
    stack.pop()
    stack.headOption match {
      case None        => writer.write(reset)
      case Some(value) => writer.write(value)
    }
  }

}
