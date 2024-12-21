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

/** Terminal effects that can change display properties. */
trait Display[+F <: Writer] extends Sgr[F] { self: F =>
  object display {
    def bold[A](f: F ?=> A): A =
      withSgr("1")(f)

    def light[A](f: F ?=> A): A =
      withSgr("2")(f)

    def invert[A](f: F ?=> A): A =
      withSgr("7")(f)
  }
}
