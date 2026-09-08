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

package terminus.ui.timer

import cats.effect.unsafe.implicits.global
import munit.FunSuite
import terminus.ui.runtime.DefaultRuntime
import terminus.ui.runtime.TestRunner

import scala.concurrent.duration.*

/** Tests [[DefaultTimer]] against a [[TestRunner]], which captures what gets
  * scheduled instead of actually running fibers. There is no more two-phase
  * task/connect dance to test: pre-run buffering is now the
  * [[terminus.ui.runtime.Runner]]'s job (its queue happily accepts offers
  * before anything is consuming them), so `DefaultTimer` itself is a thin
  * translation from `every`/`after` calls to `Runtime.schedule` calls. These
  * tests check that translation.
  */
class DefaultTimerSuite extends FunSuite:

  private def fixture(): (TestRunner, DefaultTimer) =
    val runner = new TestRunner
    val runtime = DefaultRuntime(runner)
    val timer = new DefaultTimer(runtime) {}
    (runner, timer)

  test("every returns a signal starting at zero") {
    val (_, timer) = fixture()
    val ticks = timer.every(100.millis)
    assertEquals(ticks.peek, 0L)
  }

  test("every schedules exactly one repeating stream") {
    val (runner, timer) = fixture()
    timer.every(100.millis)
    assertEquals(runner.streams.size, 1)
  }

  test("driving every's scheduled stream increments its signal") {
    val (runner, timer) = fixture()
    val ticks = timer.every(1.milli)

    val stream = runner.streams.head
    val runnables = stream.take(2).compile.toList.unsafeRunSync()
    assertEquals(runnables.size, 2)

    runnables.foreach(_.run())
    assertEquals(ticks.peek, 2L)
  }

  test("after schedules exactly one delayed task") {
    val (runner, timer) = fixture()
    timer.after(1.second)(() => ())
    assertEquals(runner.streams.size, 0)
    runner.drain() // consumes the pending IO; nothing to assert but no hang
  }

  test("after's scheduled task runs the callback once fired") {
    val (runner, timer) = fixture()
    var fired = false
    timer.after(1.milli)(() => fired = true)

    runner.drain()
    assert(fired)
  }

  test("multiple after calls each schedule independently") {
    val (runner, timer) = fixture()
    var count = 0
    timer.after(1.milli)(() => count += 1)
    timer.after(1.milli)(() => count += 1)

    runner.drain()
    assertEquals(count, 2)
  }
