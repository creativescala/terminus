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

/** Utility trait for working with stacks. */
trait WithStack[+F <: Writer] { self: F =>

  /** Use `withStack` to ensure a stack is pushed on before `f` is evaluated,
    * and popped when `f` finishes.
    */
  protected def withStack[A](stack: Stack, code: String)(f: F ?=> A): A = {
    stack.push(code, self)
    try {
      f(using this)
    } finally {
      stack.pop(self)
    }
  }
}
