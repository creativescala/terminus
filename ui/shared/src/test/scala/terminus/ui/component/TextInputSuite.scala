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

package terminus.ui.component

import munit.FunSuite
import terminus.Key
import terminus.ui.event.DefaultEvent
import terminus.ui.event.FocusId
import terminus.ui.layout.Size
import terminus.ui.react.WritableSignal
import terminus.ui.runtime.Runtime
import terminus.ui.text.Line

class TextInputSuite extends FunSuite:

  /** A TextInput wired to a runtime, as `TextInput.apply` would build it. The
    * input is the only focusable component so it receives dispatched keys
    * immediately.
    */
  private def fixture(
      initial: String
  ): (Runtime, WritableSignal[Line], TextInput) =
    val runtime = Runtime.empty
    val value = WritableSignal(Line(initial))
    val context = new DefaultEvent(FocusId.next, runtime) {}
    val input =
      new TextInput(Size.fixed(50, 3), value = value, context = context)
    (runtime, value, input)

  private def typeString(runtime: Runtime, s: String): Unit =
    s.foreach(c => runtime.dispatch(Key(c)))

  test("typing inserts at the cursor") {
    val (runtime, value, _) = fixture("")
    typeString(runtime, "hello")
    assertEquals(value.peek.value, "hello")
  }

  test(
    "an external change that shortens the value clamps the cursor at the next frame"
  ) {
    val (runtime, value, _) = fixture("")
    typeString(runtime, "hello world")
    // Cursor is now at 11, past the end of the replacement text.
    value.set(Line("hi"))

    // Without clamp-at-read Line.insert(11, _) on "hi" is out of range and
    // the keystroke is silently dropped.
    typeString(runtime, "!")
    assertEquals(value.peek.value, "hi!")
  }

  test("an external change that keeps the cursor valid leaves it in place") {
    val (runtime, value, _) = fixture("")
    typeString(runtime, "abcdef")
    (1 to 3).foreach(_ => runtime.dispatch(Key.left))
    // Cursor is at 3, still valid in the replacement text.
    value.set(Line("ABCDEF"))

    typeString(runtime, "x")
    assertEquals(value.peek.value, "ABCxDEF")
  }
