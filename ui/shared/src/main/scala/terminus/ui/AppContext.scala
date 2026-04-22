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

import terminus.Key

/** Combined capability context for interactive component bodies.
  *
  * Bundles [[RenderContext]], [[EventContext]], and [[LayoutContext]] into a
  * single capability so layout components (Row, Column) can thread all three
  * through their bodies without requiring three separate context parameters.
  *
  * Layout components create a child AppContext that forwards RenderContext and
  * EventContext from the parent while substituting their own LayoutContext.
  */
trait AppContext extends RenderContext, EventContext, LayoutContext

/** Content of a leaf component (e.g. Text).
  *
  * The [[RenderContext]] in scope enables signal reads to register the
  * component as a subscriber, so that signal changes can trigger a targeted
  * re-render.
  */
type LeafContent[A] = RenderContext ?=> A

/** Body of a layout component (e.g. Row, Column) or the top-level app body.
  *
  * Provides the full reactive + layout capability set: signals, key handlers,
  * and sub-component layout.
  */
type AppContent[A] = AppContext ?=> A

object AppContext:

  /** Creates a child AppContext that delegates [[RenderContext]] and
    * [[EventContext]] to the parent, but uses rc as the [[LayoutContext]].
    *
    * Used by layout components (Row, Column) to substitute their own layout
    * context while preserving the reactive context from the parent.
    */
  def child(parent: AppContext, rc: LayoutContext): AppContext =
    new AppContext:
      private[ui] def invalidate(): Unit = parent.invalidate()
      def createSignal[A](initial: A): Signal[A] = parent.createSignal(initial)
      def onKey(key: Key)(handler: => Unit): Unit = parent.onKey(key)(handler)
      def registerFocusable(): FocusId = parent.registerFocusable()
      private[ui] def focusedId: Option[FocusId] = parent.focusedId
      def stop(): Unit = parent.stop()
      def size: Size = rc.size
      def add(component: Component): Unit = rc.add(component)
      override def isFocused: Boolean = parent.isFocused
