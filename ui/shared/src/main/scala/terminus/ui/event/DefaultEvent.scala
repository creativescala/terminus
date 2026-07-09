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
import terminus.ui.react.Constant
import terminus.ui.react.Reactive
import terminus.ui.react.Var
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

  val focus: Reactive[Focus] =
    runtime.focusedId.map(id =>
      if id == focusId then Focus.Focused else Focus.Unfocused
    )

  // The current enabled condition. A Var of a Reactive so that enabledWhen can
  // switch the source; availability flattens through it.
  private val available: Var[Reactive[Boolean]] = Var(Constant(true))

  def enabledWhen(condition: Reactive[Boolean]): Unit =
    available.set(condition)

  val availability: Reactive[Availability] = available.flatten.map(b =>
    if b then Availability.Enabled else Availability.Disabled
  )

  // The runtime consults this predicate at focus traversal and key dispatch
  // time, outside any React context, hence peek.
  runtime.setEnabled(focusId, () => availability.peek == Availability.Enabled)

  /** A snapshot of the state variables that style selection depends on.
    *
    * Reads with peek: a component's react method must separately track the
    * underlying reactives (focus, availability) to be re-rendered when they
    * change.
    */
  def state: ComponentState =
    ComponentState(focus.peek, availability.peek)
