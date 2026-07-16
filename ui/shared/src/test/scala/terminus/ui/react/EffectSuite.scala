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

package terminus.ui.react

import munit.FunSuite
import terminus.ui.capability.React
import terminus.ui.runtime.Runtime

/** Tests effects through the application-facing surface: the [[React]]
  * capability's `signal` / `computed` / `effect` methods, with the queue
  * drained the way the event loop drains it.
  */
class EffectSuite extends FunSuite:

  /** A [[React]] capability and a drain function wired to the same runtime, as
    * an application and its event loop would see them.
    */
  private class Fixture:
    private val runtime = Runtime.empty
    val react: React = new DefaultReact(runtime) {}
    def drain(): Unit = runtime.effectQueue.drain()

  test("an effect runs once on construction") {
    val f = new Fixture
    var runs = 0
    f.react.effect { runs += 1 }
    assertEquals(runs, 1)
  }

  test("an effect re-runs on drain after a dependency changes") {
    val f = new Fixture
    val a = f.react.signal(1)
    var seen = 0
    f.react.effect { seen = a.get }
    assertEquals(seen, 1)

    a.set(2)
    assertEquals(seen, 1, "the effect must not run until the drain")
    f.drain()
    assertEquals(seen, 2)
  }

  test("an effect does not run when an unrelated signal changes") {
    val f = new Fixture
    val a = f.react.signal(1)
    val unrelated = f.react.signal(1)
    var runs = 0
    f.react.effect { a.get; runs += 1 }

    unrelated.set(2)
    f.drain()
    assertEquals(runs, 1)
  }

  test("multiple writes in a batch produce a single run") {
    val f = new Fixture
    val a = f.react.signal(1)
    val b = f.react.signal(1)
    var runs = 0
    f.react.effect { a.get; b.get; runs += 1 }

    a.set(2)
    b.set(2)
    a.set(3)
    f.drain()
    assertEquals(runs, 2)
  }

  test("an effect tracks through a computed") {
    val f = new Fixture
    val a = f.react.signal(1)
    val doubled = f.react.computed { a.get * 2 }
    var seen = 0
    f.react.effect { seen = doubled.get }
    assertEquals(seen, 2)

    a.set(3)
    f.drain()
    assertEquals(seen, 6)
  }

  test("an effect re-tracks its dependencies on every run") {
    val f = new Fixture
    val useA = f.react.signal(true)
    val a = f.react.signal("a")
    val b = f.react.signal("b")
    var seen = ""
    f.react.effect { seen = if useA.get then a.get else b.get }
    assertEquals(seen, "a")

    // Switch to b; changes to a must no longer re-run the effect.
    useA.set(false)
    f.drain()
    assertEquals(seen, "b")

    a.set("a2")
    f.drain()
    assertEquals(seen, "b", "a is no longer a dependency")

    b.set("b2")
    f.drain()
    assertEquals(seen, "b2")
  }

  test("an effect made stale while draining runs in the same drain") {
    val f = new Fixture
    val a = f.react.signal(1)
    val b = f.react.signal(0)
    var seen = 0
    // The first effect writes b, which the second effect reads.
    f.react.effect { b.set(a.get * 10) }
    f.react.effect { seen = b.get }
    assertEquals(seen, 10)

    a.set(2)
    f.drain()
    assertEquals(seen, 20)
  }
