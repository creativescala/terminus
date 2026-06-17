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

import terminus.Key

/** The reactive runtime for an interactive UI screen.
  *
  * Creates and owns [[terminus.ui.Signal]]s (their lifetime matches this
  * context's), registers key handlers, and drives re-renders when signals
  * change.
  *
  * A new Event per screen gives each screen its own isolated reactive graph and
  * key handler table, which is automatically cleaned up when the screen is
  * left.
  */
trait Event:
  /** Register a handler that fires only for the given key.
    *
    * Registering a handler automatically makes a component focusable.
    */
  def onKey(key: Key)(handler: => Unit): Unit

  /** Register a handler that fires for every key press.
    *
    * Unlike [[onKey]], which matches a specific [[Key]], this handler receives
    * the pressed [[Key]] and can inspect it freely. Handlers registered here
    * fire in addition to any matching [[onKey]] handlers for the same key.
    *
    * Registering a handler automatically makes a component focusable.
    */
  def onAnyKey(handler: Key => Unit): Unit
