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

trait RawMode {

  /** Run the given terminal program `f` in raw mode, which means that the
    * program can read user input a character at a time. In canonical mode,
    * which is the default, user input is only available a line at a time.
    */
  def raw[F <: effect.Effect, A](f: F ?=> A): (F & effect.RawMode[F]) ?=> A =
    effect ?=> effect.raw(f)
}
