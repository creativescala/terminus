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

/** Body of a layout component (e.g. Row, Column) or the top-level app body.
  *
  * Provides the full reactive + layout capability set: signals, key handlers,
  * and sub-component layout.
  */
type AppContent[A] = AppContext ?=> A

/** Content of a leaf component (e.g. Text).
  *
  * The [[RenderContext]] in scope enables signal reads to register the
  * component as a subscriber, so that signal changes can trigger a targeted
  * re-render. However, leaf content does not have access to other context and
  * so cannot, for example, create child components.
  */
type LeafContent[A] = RenderContext ?=> A
