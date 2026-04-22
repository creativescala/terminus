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

import scala.annotation.implicitNotFound

/** Reactive scope for a component render pass.
  *
  * Signal reads inside a component's content use the RenderContext in scope to
  * register the component as a subscriber, so that signal changes can trigger a
  * targeted re-render.
  *
  * For the initial full-frame re-render implementation this is a stub; the full
  * dependency-tracking mechanism will be wired in when subtree re-render is
  * implemented.
  */
@implicitNotFound(
  """signal.get requires a RenderContext, which is only available inside a
component content lambda (the body passed to Text, etc.).

If you meant to read this signal reactively, move the call inside a
component's content: Text(w, h) { mySignal.get }

If you intentionally want an untracked read in setup or event-handler
code, use signal.peek instead."""
)
trait RenderContext:
  private[ui] def invalidate(): Unit

  /** Returns true if this component is inside a focused [[FocusScope]], or if
    * it is not inside any [[FocusScope]] at all.
    *
    * Must be read in render scope (inside a component content lambda) to
    * reflect the current focus state. Reading it in setup scope captures the
    * value at construction time and will not update.
    */
  def isFocused: Boolean = true
