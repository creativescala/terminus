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

import cats.effect.IO
import fs2.Stream
import terminus.ui.capability.Timer
import terminus.ui.react.Signal
import terminus.ui.react.WritableSignal
import terminus.ui.runtime.Runtime

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
trait DefaultTimer(runtime: Runtime) extends Timer:
  private def toRunnable(f: () => Unit): Runnable =
    new Runnable:
      def run(): Unit = f()

  def every(interval: FiniteDuration): Signal[Long] =
    val ticks = WritableSignal(0L)
    runtime.schedule(
      Stream.awakeDelay[IO](interval).as((() => ticks.update(_ + 1)): Runnable)
    )
    ticks

  def after(delay: FiniteDuration)(f: () => Unit): Unit =
    runtime.schedule(IO.sleep(delay).as(toRunnable(f)))
