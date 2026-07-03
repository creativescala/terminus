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
import terminus.ui.capability.Submit
import terminus.ui.react.Constant
import terminus.ui.react.Reactive
import terminus.ui.react.Var
import terminus.ui.runtime.Runtime

class DefaultSubmitSuite extends FunSuite:

  test("the handler receives the value as of the keypress, not registration") {
    val runtime = Runtime.empty
    val value = Var("old")
    val submit =
      new DefaultSubmit[String](FocusId.next, runtime, Seq(Key.enter), value) {}

    var received: Option[String] = None
    submit.onSubmit(v => received = Some(v))

    value.set("new")
    runtime.dispatch(Key.enter)
    assertEquals(received, Some("new"))
  }

  test("a Computed value is current at submit time") {
    val runtime = Runtime.empty
    val raw = Var("query")
    val trimmed = Reactive { raw.get.trim }
    val submit =
      new DefaultSubmit[String](
        FocusId.next,
        runtime,
        Seq(Key.enter),
        trimmed
      ) {}

    var received: Option[String] = None
    submit.onSubmit(v => received = Some(v))

    raw.set("  hello  ")
    runtime.dispatch(Key.enter)
    assertEquals(received, Some("hello"))
  }

  test("every configured key activates the handler") {
    val runtime = Runtime.empty
    val submit = new DefaultSubmit[Unit](
      FocusId.next,
      runtime,
      Seq(Key.enter, Key.space),
      Constant(())
    ) {}

    var count = 0
    submit.onSubmit(_ => count += 1)

    runtime.dispatch(Key.enter)
    runtime.dispatch(Key.space)
    assertEquals(count, 2)
  }

  test("keys outside the configured set do not activate the handler") {
    val runtime = Runtime.empty
    val submit =
      new DefaultSubmit[Unit](
        FocusId.next,
        runtime,
        Seq(Key.enter),
        Constant(())
      ) {}

    var fired = false
    submit.onSubmit(_ => fired = true)

    runtime.dispatch(Key.space)
    assert(!fired)
  }

  test("the Submit[Unit] extension accepts a by-name handler") {
    val runtime = Runtime.empty
    val submit: Submit[Unit] =
      new DefaultSubmit[Unit](
        FocusId.next,
        runtime,
        Seq(Key.enter),
        Constant(())
      ) {}

    var fired = false
    submit.onSubmit { fired = true }

    runtime.dispatch(Key.enter)
    assert(fired)
  }
