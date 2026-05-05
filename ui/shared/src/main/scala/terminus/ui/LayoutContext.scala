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

/** The context that builds the component tree.
  *
  * Allows children components to register themselves with this context.
  */
trait LayoutContext:
  def size: Size
  def add(component: Component): Unit

/** The context for the root of a component tree. */
trait RootContext extends LayoutContext:
  def render(using Terminal): Unit

/** Context for any child components. */
trait ChildContext extends LayoutContext:
  def render(bounds: Rect, buf: Buffer): Unit
