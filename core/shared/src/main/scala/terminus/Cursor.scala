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

trait Cursor {
  object cursor {

    /** Move cursor to given column. The left-most column is 1, and coordinates
      * increase to the right.
      */
    def column(x: Int = 1): effect.Cursor ?=> Unit =
      effect ?=> effect.cursor.column(x)

    /** Move absolute cursor position, where (1, 1) is the top left corner and
      * coordinates increase to the right and down.
      */
    def to(x: Int, y: Int): effect.Cursor ?=> Unit =
      effect ?=> effect.cursor.to(x, y)

    /** Move the cursor position relative to the current position. Coordinates
      * are given in characters / cells.
      */
    def move(x: Int, y: Int): effect.Cursor ?=> Unit =
      effect ?=> effect.cursor.move(x, y)

    /** Move the cursor up the given number of rows. The column of the cursor
      * remains unchanged. Defaults to 1 row.
      */
    def up(lines: Int = 1): effect.Cursor ?=> Unit =
      effect ?=> effect.cursor.up(lines)

    /** Move the cursor down the given number of rows. Defaults to 1 row. */
    def down(lines: Int = 1): effect.Cursor ?=> Unit =
      effect ?=> effect.cursor.down(lines)
  }
}
