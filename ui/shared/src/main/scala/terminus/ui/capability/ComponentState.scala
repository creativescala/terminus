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

/** State record for components with a focus state. Style combinators that
  * respond to focus (e.g. `focused`) require the component's state type to
  * extend this, so styling the state of a component that doesn't have this is a
  * compile error.
  */
trait HasFocus:
  def focus: Focus

/** State record for components with an availability state. See [[HasFocus]] for
  * the convention.
  */
trait HasAvailability:
  def availability: Availability

/** A snapshot of the state variables that can affect how a component renders.
  *
  * Components feed their full state to a [[terminus.ui.style.Style]], which
  * selects the active properties. A style only has to consume the state it
  * declares an interest in.
  */
final case class ComponentState(focus: Focus, availability: Availability)
    extends HasFocus,
      HasAvailability
