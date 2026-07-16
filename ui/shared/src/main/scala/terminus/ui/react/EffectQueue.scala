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

import scala.collection.mutable

/** Collects [[Effect]]s that have become stale, to run when the current batch
  * of signal writes completes.
  *
  * Effects are not run at the moment a dependency changes: a handler that
  * writes several signals schedules its dependent effects once, and [[drain]] —
  * called by the event loop after the handler returns — runs them. Each
  * scheduled effect runs once per drain in scheduling order; an effect made
  * stale *while* draining (for example by another effect's writes) runs in the
  * same drain, so a drain always ends quiescent.
  *
  * Framework-internal: there is one queue per event loop, owned by the
  * [[terminus.ui.runtime.Runtime]]. Applications create effects through the
  * [[terminus.ui.capability.React]] capability, whose implementation routes
  * them here.
  */
private[ui] final class EffectQueue:
  private val pending = mutable.LinkedHashSet.empty[Effect]

  def schedule(effect: Effect): Unit =
    pending += effect

  /** Run scheduled effects until none remain. */
  def drain(): Unit =
    while pending.nonEmpty do
      val effect = pending.head
      pending -= effect
      effect.run()
