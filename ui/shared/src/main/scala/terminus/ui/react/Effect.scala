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

import terminus.ui.capability.Observe

import scala.collection.mutable

/** A computation run for its side effects that re-runs when a [[Signal]] it
  * reads changes — the eager sibling of [[Computed]].
  *
  * The thunk runs once on construction, subscribing to every signal it reads
  * with `get`. When any of them changes the effect does not re-run immediately;
  * it is placed on its [[EffectQueue]] and runs when the queue next drains.
  * Each run re-tracks from scratch, so dependencies may change between runs.
  *
  * The thunk should not write to a signal it also reads: the write marks the
  * effect stale again, and although an idempotent write converges (a
  * [[WritableSignal]] ignores writes of the current value), a non-idempotent
  * one loops forever.
  *
  * Effects are leaves of the reactive graph: they produce no value and nothing
  * can depend on them. Applications create them through the
  * [[terminus.ui.capability.React]] capability and never hold a reference.
  */
private[ui] final class Effect private (
    queue: EffectQueue,
    thunk: Observe ?=> Unit
) extends Listener:
  private val sources: mutable.Set[Unsubscribe] = mutable.Set.empty
  private var scheduled = false

  def setStale(): Unit =
    if !scheduled then
      scheduled = true
      queue.schedule(this)

  def addUnsubscribe(unsubscribe: Unsubscribe): Unit =
    sources += unsubscribe

  private[react] def run(): Unit =
    // Cleared before the thunk runs so a dependency change made by the thunk
    // itself (discouraged, but possible) re-schedules rather than being lost.
    scheduled = false
    // Clear sources. They will be re-registered as the thunk reads.
    sources.foreach(_.apply())
    sources.clear()

    val ctx = Observe.empty
    ctx.stack.push(this)
    thunk(using ctx)
    ctx.stack.pop()
    ()

private[ui] object Effect:
  /** Create an effect and run it immediately, establishing its subscriptions.
    */
  def apply(queue: EffectQueue)(thunk: Observe ?=> Unit): Effect =
    val effect = new Effect(queue, thunk)
    effect.run()
    effect
