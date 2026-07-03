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
import terminus.ui.capability.Event
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

  def hasFocus: Boolean = runtime.currentFocusId == focusId

  // The current enabled condition. A Var of a Reactive so that enabledWhen can
  // switch the source; enabled flattens through it.
  private val availability: Var[Reactive[Boolean]] = Var(Constant(true))

  def enabledWhen(condition: Reactive[Boolean]): Unit =
    availability.set(condition)

  val enabled: Reactive[Availability] = availability.flatten.map(b =>
    if b then Availability.Enabled else Availability.Disabled
  )

  // The runtime consults this predicate at focus traversal and key dispatch
  // time, outside any React context, hence peek.
  runtime.setEnabled(focusId, () => enabled.peek == Availability.Enabled)
