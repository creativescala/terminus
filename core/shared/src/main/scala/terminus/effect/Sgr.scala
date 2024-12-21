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

/** Provides a utility for writing SGR escape codes, which are a form of CSI
  * escape code. Intended to be extended by other effects.
  *
  * The type F is the type of effects that programs can work with.
  */
trait Sgr[+F <: Writer] extends Csi { self: F =>

  /** Run an action that is preceded by the given SGR code, and followed by a
    * reset.
    */
  protected def withSgr[A](code: String)(f: F ?=> A): A = {
    sgr(code)
    val result = f(using this)
    reset()

    result
  }

  /** Write the given SGR code. */
  protected def sgr(n: String): Unit = {
    csi("m", n)
  }

  /** Write the SGR reset code. Most operations should end with this. */
  protected def reset(): Unit =
    sgr("0")
}
