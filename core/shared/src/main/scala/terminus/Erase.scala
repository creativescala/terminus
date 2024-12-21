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

trait Erase {

  object erase {

    /** Erase the entire screen and move the cursor to the top-left. */
    def screen(): effect.Erase ?=> Unit =
      effect ?=> effect.erase.screen()

    /** Erase from current cursor position to the end of the screen. */
    def down(): effect.Erase ?=> Unit =
      effect ?=> effect.erase.down()

    /** Erase from current cursor position to the start of the screen. */
    def up(): effect.Erase ?=> Unit =
      effect ?=> effect.erase.up()

    /** Erase the current line. */
    def line(): effect.Erase ?=> Unit =
      effect ?=> effect.erase.line()
  }
}
