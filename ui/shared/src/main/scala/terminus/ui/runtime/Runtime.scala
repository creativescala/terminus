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

import terminus.Key
import terminus.ui.event.FocusId
import terminus.ui.react.EffectQueue
import terminus.ui.react.Signal
import terminus.ui.react.WritableSignal

import scala.collection.mutable

/** The runtime provides capabilties that are internal to the system and not
  * used by the application programmer.
  */
final class Runtime private ():
  // The queue on which this runtime's effects are scheduled, drained by the
  // event loop after each dispatch. Applications never see it: they create
  // effects through the React capability, whose implementation routes here.
  private[ui] val effectQueue: EffectQueue = EffectQueue()

  // The single source of truth for focus. A signal so that components can
  // derive reactive focus state from it; see [[event.DefaultEvent.focus]].
  private val currentFocus: WritableSignal[FocusId] =
    WritableSignal(FocusId.zero)

  // The root handlers get to handle events before the focused element. If they
  // handle an event it is *not* passed to the focused element.
  private var rootHandlers: Map[Key, Seq[() => Unit]] =
    Map.empty

  // The order in which we visit focusables. Follows the order in which they are
  // added.
  private val focusablesOrder: mutable.ArrayBuffer[FocusId] =
    mutable.ArrayBuffer.empty

  private val focusables: mutable.Map[FocusId, Runtime.Focusable] =
    mutable.Map.empty

  // Per-focusable predicate deciding whether it can currently be focused. A
  // focusable with no registered predicate is always enabled. Predicates are
  // read (not cached) at traversal and dispatch time so a reactive enabled
  // state takes effect immediately.
  private val enabledPredicates: mutable.Map[FocusId, () => Boolean] =
    mutable.Map.empty

  def currentFocusId: FocusId = currentFocus.peek

  /** The focused component's id, as a reactive value. */
  def focusedId: Signal[FocusId] = currentFocus

  /** Register the predicate that decides whether `focusId` can be focused. */
  def setEnabled(focusId: FocusId, predicate: () => Boolean): Unit =
    enabledPredicates(focusId) = predicate

  /** Whether `focusId` is currently enabled. Defaults to enabled when no
    * predicate has been registered.
    */
  def enabled(focusId: FocusId): Boolean =
    enabledPredicates.get(focusId).forall(_())

  def addRootHandlers(
      handlers: Map[Key, Seq[() => Unit]]
  ): Unit =
    rootHandlers = handlers

  def addKeyHandler(
      focusId: FocusId,
      key: Key,
      handler: () => Unit
  ): Unit =
    registerFocusable(focusId)
    focusables
      .getOrElseUpdate(focusId, Runtime.Focusable.empty)
      .addKeyHandler(key, handler)

  def addAnyKeyHandler(
      focusId: FocusId,
      handler: Key => Unit
  ): Unit =
    registerFocusable(focusId)
    focusables
      .getOrElseUpdate(focusId, Runtime.Focusable.empty)
      .addAnyKeyHandler(handler)

  /** Add `focusId` to the tab order the first time it registers a handler, and
    * focus it immediately if it's the first focusable seen.
    */
  private def registerFocusable(focusId: FocusId): Unit =
    if !focusablesOrder.contains(focusId) then
      focusablesOrder += focusId
      if currentFocus.peek == FocusId.zero then currentFocus.set(focusId)

  def nextFocus(): Unit = moveFocus(1)

  def prevFocus(): Unit = moveFocus(-1)

  /** Move focus to the next enabled focusable `step` positions away (positive
    * is forward, negative is backward), skipping disabled ones. If no other
    * focusable is enabled the current focus is left unchanged.
    */
  private def moveFocus(step: Int): Unit =
    val n = focusablesOrder.size
    if n == 0 then ()
    else
      // The traversal position is derived from the Var rather than cached, so
      // the Var remains the single source of truth for focus.
      val start = focusablesOrder.indexOf(currentFocus.peek).max(0)
      var offset = 1
      var found = -1
      while offset <= n && found < 0 do
        val idx = Math.floorMod(start + step * offset, n)
        if enabled(focusablesOrder(idx)) then found = idx
        offset += 1
      if found >= 0 then currentFocus.set(focusablesOrder(found))

  def dispatch(key: Key): Unit =
    rootHandlers.get(key) match
      case Some(handlers) => handlers.foreach(f => f())
      case None           =>
        // A disabled focusable swallows the key: it neither handles the event
        // itself nor lets it fall through to anything else.
        val focused = currentFocus.peek
        if enabled(focused) then
          focusables.get(focused) match
            case None            => ()
            case Some(focusable) => focusable.handle(key)

object Runtime:
  /** Store event handlers associated with a focusable component. */
  case class Focusable(
      keyHandlers: mutable.Map[Key, mutable.ArrayBuffer[() => Unit]],
      anyKeyHandlers: mutable.ArrayBuffer[Key => Unit]
  ):
    def addKeyHandler(key: Key, handler: () => Unit): Unit =
      keyHandlers.getOrElseUpdate(key, mutable.ArrayBuffer.empty) += handler

    def addAnyKeyHandler(handler: Key => Unit): Unit =
      anyKeyHandlers += handler

    def handle(key: Key): Unit =
      keyHandlers.get(key) match
        case None           => ()
        case Some(handlers) => handlers.foreach(f => f())
      anyKeyHandlers.foreach(f => f(key))

  object Focusable:
    // A def, not a val: each focusId needs its own handler tables, not a
    // shared mutable instance aliased across every focusId that hits this
    // default.
    def empty: Focusable =
      Focusable(mutable.Map.empty, mutable.ArrayBuffer.empty)

  def empty: Runtime = new Runtime()
