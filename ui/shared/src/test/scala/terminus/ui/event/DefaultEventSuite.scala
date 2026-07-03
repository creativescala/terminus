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

package terminus.ui.event

import munit.FunSuite
import terminus.Key
import terminus.ui.capability.Availability
import terminus.ui.react.Reactive
import terminus.ui.react.Var
import terminus.ui.runtime.Runtime

/** Tests that [[DefaultEvent]]'s reactive availability drives the runtime's
  * focus traversal and dispatch.
  */
class DefaultEventSuite extends FunSuite:

  private def focusable(runtime: Runtime): DefaultEvent =
    val event = new DefaultEvent(FocusId.next, runtime) {}
    // Registering a handler makes the component focusable.
    event.onKey(Key.enter) { () }
    event

  test("components are enabled by default") {
    val runtime = Runtime.empty
    val event = focusable(runtime)
    assertEquals(event.enabled.peek, Availability.Enabled)
  }

  test("enabled follows a reactive condition, including through a Computed") {
    val runtime = Runtime.empty
    val event = focusable(runtime)

    val condition = Var(false)
    // Deliberately a Computed, not the Var itself: exercises the flatten/map
    // chain and peek-on-stale recomputation.
    event.enabledWhen(Reactive { condition.get })

    assertEquals(event.enabled.peek, Availability.Disabled)
    condition.set(true)
    assertEquals(event.enabled.peek, Availability.Enabled)
  }

  test("focus traversal skips a component whose condition is false") {
    val runtime = Runtime.empty
    val a = focusable(runtime)
    val b = focusable(runtime)
    val c = focusable(runtime)

    val bEnabled = Var(false)
    b.enabledWhen(Reactive { bEnabled.get })

    assert(a.hasFocus, "first focusable starts focused")
    runtime.nextFocus()
    assert(c.hasFocus, "b is disabled and skipped")

    bEnabled.set(true)
    runtime.prevFocus()
    assert(b.hasFocus, "b rejoins the focus order when its condition changes")
  }

  test("dispatch does not deliver to a component whose condition is false") {
    val runtime = Runtime.empty
    var fired = false
    val id = FocusId.next
    val event = new DefaultEvent(id, runtime) {}
    event.onKey(Key.enter) { fired = true }
    event.enabledWhen(Var(false))

    runtime.dispatch(Key.enter)
    assert(!fired, "disabled component receives no events")
  }
