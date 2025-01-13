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

class DisplaySuite extends FunSuite {
  test("Bold and light stack in nested scopes") {
    val result =
      StringBuilderTerminal.run { t ?=>
        t.display.bold {
          t.write("Bold ")
          t.display.light { t.write("Light ") }
          t.write("Bold ")
        }
      }

    assertEquals(
      result,
      s"${AnsiCodes.display.bold.on}Bold ${AnsiCodes.display.light.on}Light ${AnsiCodes.display.bold.on}Bold ${AnsiCodes.display.bold.off}"
    )
  }

  test("Blink toggles off only when exiting outermost block") {
    val result =
      StringBuilderTerminal.run { t ?=>
        t.display.blink {
          t.write("Blink ")
          t.display.blink { t.write("Blink ") }
          t.write("Blink ")
        }
      }

    assertEquals(
      result,
      s"${AnsiCodes.display.blink.on}Blink Blink Blink ${AnsiCodes.display.blink.off}"
    )
  }
}
