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

package terminus.ui.style

import munit.FunSuite
import terminus.ui.capability.Availability
import terminus.ui.capability.ComponentState
import terminus.ui.capability.Focus

class TextInputStyleSuite extends FunSuite:

  val unfocused = ComponentState(Focus.Unfocused, Availability.Enabled)
  val focused = ComponentState(Focus.Focused, Availability.Enabled)

  test("the default cursor properties invert the cell") {
    assertEquals(
      TextInputStyle.default(unfocused).cursor,
      CellProps(invert = true)
    )
  }

  test("withCursor(f) updates the current cursor properties") {
    val style = TextInputStyle.default.withCursor(_.withBold)
    val cursor = style(unfocused).cursor
    assertEquals(cursor.bold, true)
    assertEquals(cursor.invert, true)
  }

  test("withCursor(CellProps) replaces the cursor properties") {
    val plain = CellProps(bold = true)
    val style = TextInputStyle.default.withCursor(plain)
    assertEquals(style(unfocused).cursor, plain)
  }

  test("a state rule can restyle the cursor") {
    val style = TextInputStyle.default.focused(_.withCursor(_.withBold))
    assertEquals(style(focused).cursor.bold, true)
    assertEquals(style(unfocused).cursor.bold, false)
  }
