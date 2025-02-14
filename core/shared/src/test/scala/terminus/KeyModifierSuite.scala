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

import munit.FunSuite

class KeyModifierSuite extends FunSuite {
  test("predicates return true when modifier is present") {
    assert(KeyModifier.Shift.hasShift)
    assert(KeyModifier.Control.hasControl)
    assert(KeyModifier.Alt.hasAlt)
    assert(KeyModifier.Super.hasSuper)
    assert(KeyModifier.Hyper.hasHyper)
    assert(KeyModifier.Meta.hasMeta)
  }

  test("or is the union of modifiers") {
    assert(KeyModifier.Shift.or(KeyModifier.Control).hasShift)
    assert(KeyModifier.Shift.or(KeyModifier.Control).hasControl)
  }
}
