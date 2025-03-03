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

trait AlternateScreenMode {

  /** Run the given terminal program `f` in alternate screen mode, which means
    * that whatever is displayed by `f` will not been shown when the program
    * exits, and similarly key presses will not be saved in the history buffer.
    */
  def alternateScreen[F <: effect.Effect, A](
      f: F ?=> A
  ): (F & effect.AlternateScreenMode[F]) ?=> A =
    effect ?=> effect.alternateScreen(f)
}
