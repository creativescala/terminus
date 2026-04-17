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

/** Reactive scope for a component render pass.
  *
  * Signal reads inside a component's content use the ComponentContext in scope
  * to register the component as a subscriber, so that signal changes can
  * trigger a targeted re-render.
  *
  * For the initial full-frame re-render implementation this is a stub; the full
  * dependency-tracking mechanism will be wired in when subtree re-render is
  * implemented.
  */
trait ComponentContext:
  private[ui] def invalidate(): Unit
