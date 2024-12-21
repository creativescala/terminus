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

/** Functionality for scrolling the terminal. */
trait Scroll extends Csi {
  object scroll {

    /** Scroll the display up the given number of rows. Defaults to 1 row. */
    def up(lines: Int = 1): Unit =
      (0.until(lines)).foreach(_ => csi("S"))

    /** Scroll the display down the given number of rows. Defaults to 1 row. */
    def down(lines: Int = 1): Unit =
      (0.until(lines)).foreach(_ => csi("T"))
  }

}
