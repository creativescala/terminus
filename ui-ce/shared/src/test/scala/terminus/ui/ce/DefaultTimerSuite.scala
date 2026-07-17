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

package terminus.ui.ce

import munit.FunSuite

import scala.collection.mutable
import scala.concurrent.duration.*

class DefaultTimerSuite extends FunSuite:
  import DefaultTimer.Task

  test("tasks scheduled before connect are replayed on connect, in order") {
    val timer = DefaultTimer()
    val ticks = timer.every(100.millis)
    timer.after(1.second)(() => ())

    val spawned = mutable.ListBuffer.empty[Task]
    timer.connect(spawned += _)

    spawned.toList match
      case List(Task.Every(interval, signal), Task.After(delay, _)) =>
        assertEquals(interval, 100.millis: FiniteDuration)
        assertEquals(delay, 1.second: FiniteDuration)
        // The task carries the same signal every returned, so ticks served
        // from the task are visible through it.
        signal.update(_ + 1)
        assertEquals(ticks.peek, 1L)
      case other => fail(s"Unexpected tasks: $other")
  }

  test("tasks scheduled after connect spawn immediately") {
    val timer = DefaultTimer()
    val spawned = mutable.ListBuffer.empty[Task]
    timer.connect(spawned += _)
    assert(spawned.isEmpty)

    timer.after(1.second)(() => ())
    assertEquals(spawned.size, 1)
  }

  test("replayed tasks are not replayed twice") {
    val timer = DefaultTimer()
    timer.after(1.second)(() => ())

    val spawned = mutable.ListBuffer.empty[Task]
    timer.connect(spawned += _)
    assertEquals(spawned.size, 1)

    timer.after(2.seconds)(() => ())
    assertEquals(spawned.size, 2)
  }
