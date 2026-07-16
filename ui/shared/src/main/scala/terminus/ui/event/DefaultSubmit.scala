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
import terminus.ui.capability.Submit
import terminus.ui.react.Signal
import terminus.ui.runtime.Runtime

/** The default implementation of the [[terminus.ui.capability.Submit]]
  * capability, which binds activation to the given `keys` via
  * [[terminus.ui.runtime.Runtime]]. The component chooses its activation keys:
  * Enter and Space for a button, but only Enter for a text input, where Space
  * must insert a space. The submitted value is read from `currentValue` at the
  * moment a key fires, so the handler sees the value as of the keypress, not as
  * of registration.
  *
  * Registering a handler makes the component focusable; the runtime declines to
  * deliver these keys while the component is disabled (see
  * [[terminus.ui.capability.Event.enabledWhen]]).
  */
trait DefaultSubmit[A](
    focusId: FocusId,
    runtime: Runtime,
    keys: Seq[Key],
    currentValue: Signal[A]
) extends Submit[A]:

  def onSubmit(handler: A => Unit): Unit =
    keys.foreach { key =>
      runtime.addKeyHandler(focusId, key, () => handler(currentValue.peek))
    }
