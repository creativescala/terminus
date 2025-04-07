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

trait WithEffect[+F <: Writer] { self: F =>
  protected def withEffect[A](on: String, off: String)(f: F ?=> A): A = {
    write(on)
    try {
      f(using this)
    } finally {
      write(off)
    }
  }

  protected def withEffect[A](on: String)(f: F ?=> A): A = {
    write(on)
    f(using this)
  }

  protected def effectDeferRollback(on: String, off: String): () => Unit = {
    write(on)
    () => write(off)
  }
}
