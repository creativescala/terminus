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

class AnsiCodesSuite extends FunSuite {
  test("csi is the correct code") {
    assertEquals(AnsiCodes.csiCode, "[")
  }

  test("csi strings with no arguments are correctly constructed") {
    assertEquals(AnsiCodes.csi("A"), "[A")
  }

  test("csi strings with a single argument are correctly constructed") {
    assertEquals(AnsiCodes.csi("m", "49"), "[49m")
  }

  test("csi strings with multiple arguments are correctly constructed") {
    assertEquals(AnsiCodes.csi("m", "4", "1"), "[4;1m")
  }
}
