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

package terminus.ui

/** Opaque token identifying a focusable component, returned by
  * [[EventContext.registerFocusable]]. Used by [[FocusScope]] to gate key
  * handlers on whether this scope is currently focused.
  */
opaque type FocusId = Int

private[ui] object FocusId:
  def apply(id: Int): FocusId = id
