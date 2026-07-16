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

import terminus.Key
import terminus.ui.capability.Availability
import terminus.ui.capability.ComponentState
import terminus.ui.capability.Event
import terminus.ui.capability.Focus
import terminus.ui.capability.Observe
import terminus.ui.react.Computed
import terminus.ui.react.Signal
import terminus.ui.react.WritableSignal
import terminus.ui.runtime.Runtime

/** The default implementation of the [[terminus.ui.capability.Event]]
  * capability, which defers to [[terminus.ui.runtime.Runtime]] for its
  * implementation.
  */
trait DefaultEvent(focusId: FocusId, runtime: Runtime) extends Event:

  def onKey(key: Key)(handler: => Unit): Unit =
    runtime.addKeyHandler(focusId, key, () => handler)

  def onAnyKey(handler: Key => Unit): Unit =
    runtime.addAnyKeyHandler(focusId, handler)

  def nextFocus(): Unit = runtime.nextFocus()

  def prevFocus(): Unit = runtime.prevFocus()

  // Framework code constructs Computed directly rather than going through the
  // React capability: these signals belong to the context itself, not to
  // anything the application created.
  val focus: Signal[Focus] = Computed {
    if runtime.focusedId.get == focusId then Focus.Focused
    else Focus.Unfocused
  }

  // The current enabled condition. A signal of a signal so that enabledWhen
  // can switch the source; availability reads through both layers.
  private val available: WritableSignal[Signal[Boolean]] =
    WritableSignal(Signal.constant(true))

  def enabledWhen(condition: Signal[Boolean]): Unit =
    available.set(condition)

  val availability: Signal[Availability] = Computed {
    if available.get.get then Availability.Enabled else Availability.Disabled
  }

  // The runtime consults this predicate at focus traversal and key dispatch
  // time, outside any tracked computation, hence peek.
  runtime.setEnabled(focusId, () => availability.peek == Availability.Enabled)

  /** A snapshot of the state variables that style selection depends on.
    *
    * Reads with get: resolving a style during measure or render subscribes the
    * enclosing render effect to focus and availability, so a change to either
    * triggers the next frame.
    */
  def state(using Observe): ComponentState =
    ComponentState(focus.get, availability.get)
