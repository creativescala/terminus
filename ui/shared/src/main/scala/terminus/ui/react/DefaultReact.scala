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
import terminus.ui.capability.React
import terminus.ui.runtime.Runtime

/** The default implementation of the [[terminus.ui.capability.React]]
  * capability. Creation does the framework-side wiring the application never
  * sees: effects are routed to the runtime's effect queue, which the event loop
  * drains. When ownership is implemented, this is also where created values
  * will be registered for disposal with their owning component.
  */
trait DefaultReact(runtime: Runtime) extends React:
  def signal[A](initial: A): WritableSignal[A] =
    WritableSignal(initial)

  def computed[A](thunk: Observe ?=> A): Signal[A] =
    Computed(thunk)

  def effect(thunk: Observe ?=> Unit): Unit =
    Effect(runtime.effectQueue)(thunk)
    ()
