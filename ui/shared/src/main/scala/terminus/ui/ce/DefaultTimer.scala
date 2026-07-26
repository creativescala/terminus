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

import terminus.ui.capability.Timer
import terminus.ui.react.Signal
import terminus.ui.react.WritableSignal

import scala.collection.mutable
import scala.concurrent.duration.FiniteDuration

/** The [[Timer]] implementation for the Cats Effect runner.
  *
  * Timers are requested when the application is constructed (setup scope) or
  * from event handlers, but the fibers that serve them can only exist once the
  * runner's session is live. So this is a two-phase object: tasks scheduled
  * before [[connect]] are recorded, and connecting replays them and switches to
  * spawning directly. The runner's spawn callback offers each tick to the event
  * queue as an `Event.Effect`, so timer writes reach the reactive graph on the
  * loop like every other write.
  */
private[ui] final class DefaultTimer extends Timer:
  import DefaultTimer.Task

  private val pending = mutable.ArrayBuffer.empty[Task]
  private var spawn: Option[Task => Unit] = None

  def every(interval: FiniteDuration): Signal[Long] =
    val ticks = WritableSignal(0L)
    schedule(Task.Every(interval, ticks))
    ticks

  def after(delay: FiniteDuration)(f: () => Unit): Unit =
    schedule(Task.After(delay, f))

  // Synchronized because handlers schedule from the session's consumer while
  // connect arrives from session setup.
  private def schedule(task: Task): Unit = synchronized {
    spawn match
      case Some(run) => run(task)
      case None      => pending.addOne(task): Unit
  }

  /** Start serving tasks: replay everything scheduled so far through `run`, and
    * pass future tasks to it directly.
    */
  private[ce] def connect(run: Task => Unit): Unit = synchronized {
    spawn = Some(run)
    pending.foreach(run)
    pending.clear()
  }

private[ui] object DefaultTimer:
  private[ce] enum Task:
    case Every(interval: FiniteDuration, ticks: WritableSignal[Long])
    case After(delay: FiniteDuration, run: () => Unit)
