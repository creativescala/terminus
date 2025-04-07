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

trait AlternateScreenMode[+F <: Effect] { self: F =>

  /** Run the given terminal program `f` in alternate screen mode, which means
    * that whatever is displayed by `f` will not been shown when the program
    * exits, and similarly key presses will not be saved in the history buffer.
    *
    * This is a low level method that is slightly dangerous. Care must be taken
    * to ensure raw mode is closed and as such, this method is package private
    * to avoid exposure.
    */
  private[terminus] def setAlternateScreenMode(): () => Unit
}
