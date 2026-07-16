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
import terminus.ui.react.WritableSignal

import scala.annotation.implicitNotFound

/** The capability to create reactive values: signals, computed signals, and
  * effects.
  *
  * A React is in scope in setup code — the body passed to
  * [[terminus.ui.FullScreen]] or to a component — which is where reactive state
  * belongs. All reactive values are created through this capability so the
  * implementation can do the framework-side wiring they need (routing effects
  * to the event loop's queue and, in future, registering everything created for
  * disposal with its owning component). Application code never sees those
  * concerns.
  *
  * It is deliberately unavailable inside tracked computations (see
  * [[Observe]]): a reactive value created during a render pass would be
  * recreated on every frame and never disposed.
  */
@implicitNotFound(
  """Creating a signal, computed, or effect requires a React capability, which
is only available in setup code: the body passed to FullScreen or to a
component such as Column or Text. Move the creation there.

Reactive values must not be created inside a render pass or another reactive
thunk — they would be recreated on every run and never disposed."""
)
trait React:
  /** Create a writable signal holding `initial`. */
  def signal[A](initial: A): WritableSignal[A]

  /** Create a signal with a constant value. This doesn't need the React
    * capability, but is nonetheless provided by this capability so the API is
    * uniform: all signals can be constructed by calls to a React instance.
    */
  def constant[A](value: A): Signal[A] =
    Signal.constant(value)

  /** Create a signal derived from other signals: `thunk` re-evaluates when a
    * signal it reads changes.
    */
  def computed[A](thunk: Observe ?=> A): Signal[A]

  /** Create an effect: `thunk` runs now, and re-runs when a signal it reads
    * changes. Re-runs are batched: they happen when the event loop next drains
    * its effect queue, not at the instant of the write.
    */
  def effect(thunk: Observe ?=> Unit): Unit
