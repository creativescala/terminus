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

import scala.collection.mutable

/** The reactive runtime for an interactive UI screen.
  *
  * Creates and owns [[Signal]]s (their lifetime matches this context's),
  * registers key handlers, and drives re-renders when signals change.
  *
  * A new EventContext per screen gives each screen its own isolated reactive
  * graph and key handler table, which is automatically cleaned up when the
  * screen is left.
  */
trait EventContext:
  def createSignal[A](initial: A): Signal[A]
  def onKey(key: Key)(handler: => Unit): Unit

  /** Register a new focusable scope and return its [[FocusId]].
    *
    * Scopes are registered in call order, which determines the Tab traversal
    * order. The first registered scope receives focus initially.
    */
  def registerFocusable(): FocusId

  /** The [[FocusId]] of the currently focused scope, or [[None]] if no
    * focusable scopes have been registered.
    */
  private[ui] def focusedId: Option[FocusId]

  /** Stop the event loop, causing [[FullScreen.run]] to return. */
  def stop(): Unit

private[ui] final class EventContextImpl extends EventContext:
  private var _needsRerender: Boolean = false
  private var _running: Boolean = true
  private val keyHandlers: mutable.Map[Key, mutable.ListBuffer[() => Unit]] =
    mutable.Map.empty

  private var _nextFocusId: Int = 0
  private val _focusables: mutable.ListBuffer[FocusId] =
    mutable.ListBuffer.empty
  private var _focusedId: Option[FocusId] = None

  // Wire Tab cycling at construction time so it is always available.
  onKey(Key.tab) { advanceFocus() }
  onKey(Key.backTab) { retreatFocus() }

  private[ui] def needsRerender: Boolean = _needsRerender
  private[ui] def running: Boolean = _running
  private[ui] def clearRerender(): Unit = _needsRerender = false
  private[ui] def scheduleRerender(): Unit = _needsRerender = true

  def createSignal[A](initial: A): Signal[A] =
    new SignalImpl(initial, this)

  def onKey(key: Key)(handler: => Unit): Unit =
    keyHandlers.getOrElseUpdate(key, mutable.ListBuffer.empty) += (() =>
      handler
    )

  def registerFocusable(): FocusId =
    val id = FocusId(_nextFocusId)
    _nextFocusId += 1
    _focusables += id
    if _focusedId.isEmpty then _focusedId = Some(id)
    id

  private[ui] def focusedId: Option[FocusId] = _focusedId

  def stop(): Unit = _running = false

  private def advanceFocus(): Unit =
    if _focusables.size > 1 then
      _focusedId match
        case None =>
          _focusedId = _focusables.headOption
          scheduleRerender()
        case Some(current) =>
          val next = (_focusables.indexOf(current) + 1) % _focusables.size
          _focusedId = Some(_focusables(next))
          scheduleRerender()

  private def retreatFocus(): Unit =
    if _focusables.size > 1 then
      _focusedId match
        case None =>
          _focusedId = _focusables.lastOption
          scheduleRerender()
        case Some(current) =>
          val idx = _focusables.indexOf(current)
          val prev = (idx - 1 + _focusables.size) % _focusables.size
          _focusedId = Some(_focusables(prev))
          scheduleRerender()

  private[ui] def dispatch(key: Key): Unit =
    keyHandlers.get(key).foreach(_.foreach(_.apply()))
