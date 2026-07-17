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

package terminus.ui.capability

import terminus.ui.react.Signal

import scala.concurrent.duration.FiniteDuration

/** The capability to make things happen at a time, rather than in response to a
  * key press.
  *
  * Only runtimes with a notion of time provide this capability — the Cats
  * Effect runner in ui-ce does, the blocking runner does not — which is how an
  * application that needs timers says so at compile time.
  */
trait Timer:
  /** A signal that counts timer ticks, incremented every `interval`. Belongs in
    * setup scope, like all reactive value creation: derive spinner frames,
    * blink state, and the like from it with `computed`.
    */
  def every(interval: FiniteDuration): Signal[Long]

  /** Run `f` on the event loop after `delay`. Usable from setup scope and from
    * event handlers alike: a button's pressed-flash is
    * `after(200.millis)(() => pressed.set(false))` in its submit handler.
    */
  def after(delay: FiniteDuration)(f: () => Unit): Unit
