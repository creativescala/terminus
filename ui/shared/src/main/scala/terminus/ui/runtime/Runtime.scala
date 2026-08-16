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

package terminus.ui.runtime

import cats.effect.IO
import fs2.Stream
import terminus.Key
import terminus.ui.capability.Schedule
import terminus.ui.event.FocusId
import terminus.ui.react.Effect
import terminus.ui.react.Signal

/** The runtime provides capabilties that are internal to the system and not
  * used by the application programmer.
  *
  * Access to the Runtime should be single-threaded, as it contains a lot of
  * mutable state.
  */
trait Runtime extends Schedule:

  /** Allows an application to request that the Runtime quit at the next safe
    * point to do so.
    */
  def quit(): Unit

  /** True if the Runtime has been asked to quit. */
  def quitRequested: Boolean

  def schedule(io: IO[Runnable]): Unit

  def schedule(stream: Stream[IO, Runnable]): Unit

  def schedule(effect: Effect): Unit

  def currentFocusId: FocusId

  /** The focused component's id, as a reactive value. */
  def focusedId: Signal[FocusId]

  /** Register the predicate that decides whether `focusId` can be focused. */
  def setEnabled(focusId: FocusId, predicate: () => Boolean): Unit

  /** Whether `focusId` is currently enabled. Defaults to enabled when no
    * predicate has been registered.
    */
  def enabled(focusId: FocusId): Boolean

  def addRootHandlers(
      handlers: Map[Key, Seq[() => Unit]]
  ): Unit

  def addKeyHandler(
      focusId: FocusId,
      key: Key,
      handler: () => Unit
  ): Unit

  def addAnyKeyHandler(
      focusId: FocusId,
      handler: Key => Unit
  ): Unit

  def nextFocus(): Unit

  def prevFocus(): Unit

  def dispatch(key: Key): Unit
