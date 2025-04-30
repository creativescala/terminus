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

import munit.FunSuite
import terminus.StringBuilderTerminal

class CursorSuite extends FunSuite {

  test("cursor.left moves cursor left by default 1 column") {
    val terminal = new StringBuilderTerminal()
    terminal.cursor.left()
    assertEquals(terminal.result(), s"${Ascii.ESC}[1D")
  }
  
  test("cursor.left moves cursor left by specified columns") {
    val terminal = new StringBuilderTerminal()
    terminal.cursor.left(5)
    assertEquals(terminal.result(), s"${Ascii.ESC}[5D")
  }
  
  test("cursor.right moves cursor right by default 1 column") {
    val terminal = new StringBuilderTerminal()
    terminal.cursor.right()
    assertEquals(terminal.result(), s"${Ascii.ESC}[1C")
  }
  
  test("cursor.right moves cursor right by specified columns") {
    val terminal = new StringBuilderTerminal()
    terminal.cursor.right(3)
    assertEquals(terminal.result(), s"${Ascii.ESC}[3C")
  }
  
  test("all cursor movement methods emit correct ANSI codes") {
    val terminal = new StringBuilderTerminal()
    
    terminal.cursor.up()
    val up = terminal.result()
    assertEquals(up, s"${Ascii.ESC}[1A")
    
    terminal.cursor.down()
    val down = terminal.result()
    assertEquals(down, s"${Ascii.ESC}[1B")
    
    terminal.cursor.right()
    val right = terminal.result()
    assertEquals(right, s"${Ascii.ESC}[1C")
    
    terminal.cursor.left()
    val left = terminal.result()
    assertEquals(left, s"${Ascii.ESC}[1D")
  }
}