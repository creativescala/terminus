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

/** Functionality for manipulating the terminal's cursor. */
trait Cursor extends Writer {
  object cursor {

    /** Move cursor to given column. The left-most column is 1, and coordinates
      * increase to the right.
      */
    def column(x: Int = 1): Unit =
      write(AnsiCodes.cursor.column(x))

    /** Move absolute cursor position, where (1, 1) is the top left corner and
      * coordinates increase to the right and down.
      */
    def to(x: Int, y: Int): Unit =
      write(AnsiCodes.cursor.to(x, y))

    /** Move the cursor position relative to the current position. Coordinates
      * are given in characters / cells.
      */
    def move(x: Int, y: Int): Unit = {
      if x < 0 then write(AnsiCodes.cursor.backward(-x))
      else write(AnsiCodes.cursor.forward(x))

      if y < 0 then write(AnsiCodes.cursor.up(-y))
      else write(AnsiCodes.cursor.down(y))
    }

    /** Move the cursor up the given number of rows. The column of the cursor
      * remains unchanged. Defaults to 1 row.
      */
    def up(lines: Int = 1): Unit =
      write(AnsiCodes.cursor.up(lines))

    /** Move the cursor down the given number of rows. Defaults to 1 row. */
    def down(lines: Int = 1): Unit =
      write(AnsiCodes.cursor.down(lines))
      
    /** Move the cursor right the given number of columns. Defaults to 1 column. */
    def right(columns: Int = 1): Unit =
      write(AnsiCodes.cursor.forward(columns))
      
    /** Move the cursor left the given number of columns. Defaults to 1 column. */
    def left(columns: Int = 1): Unit =
      write(AnsiCodes.cursor.backward(columns))
  }
}
