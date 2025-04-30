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

class CursorSuite extends FunSuite {
  
  // Test implementation for Writer trait to record what's written
  trait TestWriter extends Writer {
    var written: String = ""
    def write(str: String): Unit = written += str
    def write(char: Char): Unit = written += char
    def flush(): Unit = ()
    def clear(): Unit = written = ""
  }
  
  // Test implementation for Cursor that uses our TestWriter
  class TestCursor extends Cursor with TestWriter

  test("cursor.left moves cursor left by default 1 column") {
    val cursor = new TestCursor()
    cursor.cursor.left()
    assertEquals(cursor.written, s"${Ascii.ESC}[1D")
  }
  
  test("cursor.left moves cursor left by specified columns") {
    val cursor = new TestCursor()
    cursor.cursor.left(5)
    assertEquals(cursor.written, s"${Ascii.ESC}[5D")
  }
  
  test("cursor.right moves cursor right by default 1 column") {
    val cursor = new TestCursor()
    cursor.cursor.right()
    assertEquals(cursor.written, s"${Ascii.ESC}[1C")
  }
  
  test("cursor.right moves cursor right by specified columns") {
    val cursor = new TestCursor()
    cursor.cursor.right(3)
    assertEquals(cursor.written, s"${Ascii.ESC}[3C")
  }
  
  test("all cursor movement methods emit correct ANSI codes") {
    val cursor = new TestCursor()
    
    cursor.clear()
    cursor.cursor.up()
    assertEquals(cursor.written, s"${Ascii.ESC}[1A")
    
    cursor.clear()
    cursor.cursor.down()
    assertEquals(cursor.written, s"${Ascii.ESC}[1B")
    
    cursor.clear()
    cursor.cursor.right()
    assertEquals(cursor.written, s"${Ascii.ESC}[1C")
    
    cursor.clear()
    cursor.cursor.left()
    assertEquals(cursor.written, s"${Ascii.ESC}[1D")
  }
}