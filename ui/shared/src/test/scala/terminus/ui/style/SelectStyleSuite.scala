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

class SelectStyleSuite extends FunSuite:

  val unfocused = ComponentState(Focus.Unfocused, Availability.Enabled)
  val focused = ComponentState(Focus.Focused, Availability.Enabled)

  test("the default selected properties invert the cell") {
    assertEquals(
      SelectStyle.default(unfocused).selected,
      CellProps(invert = true)
    )
  }

  test("withSelected(f) updates the current selected properties") {
    val style = SelectStyle.default.withSelected(_.withBold)
    val selected = style(unfocused).selected
    assertEquals(selected.bold, true)
    assertEquals(selected.invert, true)
  }

  test("withSelected(CellProps) replaces the selected properties") {
    val plain = CellProps(bold = true)
    val style = SelectStyle.default.withSelected(plain)
    assertEquals(style(unfocused).selected, plain)
  }

  test("a state rule can restyle the selected item") {
    val style = SelectStyle.default.focused(_.withSelected(_.withBold))
    assertEquals(style(focused).selected.bold, true)
    assertEquals(style(unfocused).selected.bold, false)
    // The base selected properties still apply in both states.
    assertEquals(style(focused).selected.invert, true)
    assertEquals(style(unfocused).selected.invert, true)
  }
