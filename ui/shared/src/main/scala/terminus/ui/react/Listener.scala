/*
 * Copyright 2026 Creative Scala
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

package terminus.ui.react

/** A Listener listens to changes in the reactive state and dependency graph,
  * and provides the operations necessary to maintain the dependency graph.
  */
trait Listener:
  /** Set the listener's state to stale, and recursively do the same to all its
    * subscribers.
    */
  def setStale(): Unit

  /** Add an Unsubscribe to this Listener's set of sources.
    */
  def addUnsubscribe(unsubscribe: Unsubscribe): Unit
