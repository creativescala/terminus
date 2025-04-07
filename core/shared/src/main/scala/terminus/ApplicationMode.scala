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

trait ApplicationMode {

  /** Run the given terminal program `f` in application mode, which changes the
    * input sent to the program when arrow keys are pressed. See
    * https://invisible-island.net/xterm/xterm.faq.html#xterm_arrows
    */
  def application[F <: effect.Effect, A](
      f: F ?=> A
  ): (F & effect.ApplicationMode[F]) ?=> A =
    effect ?=>
      val finalizer = effect.setApplicationMode()

      try {
        f(using effect)
      } finally {
        finalizer()
      }
}
