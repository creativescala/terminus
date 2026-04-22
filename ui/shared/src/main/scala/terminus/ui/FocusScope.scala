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

/** A focus scope groups a set of components under a single [[FocusId]].
  *
  * Key handlers registered with [[EventContext.onKey]] inside a FocusScope only
  * fire when this scope is the currently focused one. Tab / Shift-Tab cycle
  * focus between registered scopes in registration order.
  *
  * Components inside the scope can call [[AppContext.isFocused]] to query focus
  * state during rendering (e.g. to highlight a border).
  */
object FocusScope:
  def apply[A](f: AppContent[A])(using parent: AppContext): A =
    val id = parent.registerFocusable()

    given AppContext = new AppContext:
      private[ui] def invalidate(): Unit = parent.invalidate()
      def createSignal[A](initial: A): Signal[A] = parent.createSignal(initial)
      def registerFocusable(): FocusId = parent.registerFocusable()
      private[ui] def focusedId: Option[FocusId] = parent.focusedId
      def onKey(key: Key)(handler: => Unit): Unit =
        parent.onKey(key) { if isFocused then handler }
      def stop(): Unit = parent.stop()
      def size: Size = parent.size
      def add(component: Component): Unit = parent.add(component)
      override def isFocused: Boolean = parent.focusedId == Some(id)
    f
