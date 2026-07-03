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

package terminus.ui.runtime

import munit.FunSuite
import terminus.Key
import terminus.ui.event.FocusId

/** Tests focused on [[Runtime]] focus traversal and event dispatch, in
  * particular how the enabled/disabled state interacts with both.
  */
class RuntimeSuite extends FunSuite:

  /** Register three focusables that each record activation into `fired`, and
    * return their ids in registration (tab) order.
    */
  private def threeFocusables(
      runtime: Runtime,
      fired: scala.collection.mutable.Set[Int]
  ): (FocusId, FocusId, FocusId) =
    val a, b, c = FocusId.next
    List(a, b, c).zipWithIndex.foreach { (id, i) =>
      runtime.addKeyHandler(id, Key.enter, () => fired += i)
    }
    (a, b, c)

  test("first registered focusable is focused") {
    val runtime = Runtime.empty
    val (a, _, _) = threeFocusables(runtime, scala.collection.mutable.Set.empty)
    assertEquals(runtime.currentFocusId, a)
  }

  test("nextFocus skips a disabled focusable") {
    val runtime = Runtime.empty
    val (a, b, c) = threeFocusables(runtime, scala.collection.mutable.Set.empty)
    runtime.setEnabled(b, () => false)

    assertEquals(runtime.currentFocusId, a)
    runtime.nextFocus()
    assertEquals(runtime.currentFocusId, c, "b is disabled and skipped")
    runtime.nextFocus()
    assertEquals(runtime.currentFocusId, a, "wraps around, still skipping b")
  }

  test("prevFocus skips a disabled focusable") {
    val runtime = Runtime.empty
    val (a, b, c) = threeFocusables(runtime, scala.collection.mutable.Set.empty)
    runtime.setEnabled(b, () => false)

    runtime.prevFocus()
    assertEquals(runtime.currentFocusId, c)
    runtime.prevFocus()
    assertEquals(runtime.currentFocusId, a, "b is disabled and skipped")
  }

  test("focus is left unchanged when no other focusable is enabled") {
    val runtime = Runtime.empty
    val (a, b, c) = threeFocusables(runtime, scala.collection.mutable.Set.empty)
    runtime.setEnabled(b, () => false)
    runtime.setEnabled(c, () => false)

    runtime.nextFocus()
    assertEquals(runtime.currentFocusId, a)
  }

  test("a reactive predicate takes effect immediately") {
    val runtime = Runtime.empty
    val (a, b, c) = threeFocusables(runtime, scala.collection.mutable.Set.empty)
    var bEnabled = false
    runtime.setEnabled(b, () => bEnabled)

    runtime.nextFocus()
    assertEquals(runtime.currentFocusId, c, "b starts disabled")

    bEnabled = true
    runtime.prevFocus()
    assertEquals(runtime.currentFocusId, b, "b is now enabled")
  }

  test("dispatch delivers to the enabled focused component") {
    val runtime = Runtime.empty
    val fired = scala.collection.mutable.Set.empty[Int]
    threeFocusables(runtime, fired)

    runtime.dispatch(Key.enter)
    assertEquals(fired.toSet, Set(0))
  }

  test("dispatch is swallowed while the focused component is disabled") {
    val runtime = Runtime.empty
    val fired = scala.collection.mutable.Set.empty[Int]
    val (a, _, _) = threeFocusables(runtime, fired)
    runtime.setEnabled(a, () => false)

    runtime.dispatch(Key.enter)
    assertEquals(
      fired.toSet,
      Set.empty[Int],
      "disabled focus receives no events"
    )
  }

  test("an unregistered focusable defaults to enabled") {
    val runtime = Runtime.empty
    val fired = scala.collection.mutable.Set.empty[Int]
    threeFocusables(runtime, fired)

    // No setEnabled call for the focused component.
    runtime.dispatch(Key.enter)
    assertEquals(fired.toSet, Set(0))
  }
