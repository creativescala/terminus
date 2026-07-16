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

package terminus.ui.react

import terminus.ui.capability.Observe
import terminus.ui.capability.React

import scala.collection.mutable
import scala.compiletime.uninitialized

/** A value that may change over time, notifying its dependents when it does.
  * Signals are created through the [[terminus.ui.capability.React]] capability
  * (or, for values that never change, [[Signal.constant]]).
  */
sealed trait Signal[A]:
  private[ui] def state: State

  /** Read the signal's current value without registering a dependency. The
    * value is current: a stale computed signal recomputes before answering, but
    * the caller is not subscribed to future changes. Use this in setup and
    * event-handler code.
    */
  def peek: A

  /** Read the signal's current value, subscribing the enclosing tracked
    * computation (see [[terminus.ui.capability.Observe]]) and recomputing if
    * necessary.
    */
  def get(using ctx: Observe): A

  def map[B](f: A => B)(using r: React): Signal[B] =
    r.computed { f(this.get) }

object Signal:
  /** A signal that never changes. Needs no [[React]] capability: a constant
    * subscribes to nothing and notifies nobody, so there is nothing to wire or
    * dispose.
    */
  def constant[A](value: A): Signal[A] = Constant(value)

  extension [A](nested: Signal[Signal[A]])
    def flatten(using r: React): Signal[A] =
      r.computed { nested.get.get }

/** A [[Signal]] whose value can be set directly. Created with the `signal`
  * method on the [[terminus.ui.capability.React]] capability.
  */
final class WritableSignal[A] private[ui] (private var currentValue: A)
    extends Signal[A]:
  private val subscribers: mutable.Set[Listener] = mutable.Set.empty

  private[ui] val state: State = State.Fresh

  def update(f: A => A): Unit =
    set(f(currentValue))

  def set(newValue: A): Unit =
    if currentValue == newValue then ()
    else
      currentValue = newValue
      subscribers.foreach(_.setStale())

  def peek: A =
    currentValue

  def get(using ctx: Observe): A =
    ctx.stack.headOption.foreach { handle =>
      subscribers += handle
      handle.addUnsubscribe(Unsubscribe(handle, subscribers))
    }
    currentValue
object WritableSignal:
  private[ui] def apply[A](initial: A): WritableSignal[A] =
    new WritableSignal(initial)

/** A [[Signal]] that never changes. */
private[ui] final class Constant[A](val peek: A) extends Signal[A]:
  private[ui] def state: State = State.Fresh

  def get(using ctx: Observe): A =
    peek
private[ui] object Constant:
  def apply[A](value: A): Signal[A] = new Constant(value)

/** A [[Signal]] derived from other signals: `thunk` re-evaluates, lazily, when
  * a signal it read has changed. Created with the `computed` method on the
  * [[terminus.ui.capability.React]] capability.
  */
private[ui] final class Computed[A](thunk: Observe ?=> A)
    extends Signal[A],
      Listener:
  private val subscribers: mutable.Set[Listener] = mutable.Set.empty
  private val sources: mutable.Set[Unsubscribe] = mutable.Set.empty

  private var cachedValue: A = uninitialized
  private[ui] var state: State = State.Stale

  def setStale(): Unit =
    if state == State.Stale then ()
    else
      state = State.Stale
      subscribers.foreach(_.setStale())

  def addUnsubscribe(unsubscribe: Unsubscribe): Unit =
    sources += unsubscribe

  protected def update()(using ctx: Observe): Unit =
    // Clear sources. They will be recomputed as parents run.
    sources.foreach(_.apply())
    sources.clear()

    ctx.stack.push(this)

    cachedValue = thunk(using ctx)
    state = State.Fresh
    ctx.stack.pop()
    ()

  def peek: A =
    // Recompute if stale so peek never observes an uninitialized or outdated
    // cache. The empty Observe context means the caller is not subscribed, but
    // this Computed still tracks its own sources (update pushes it onto the
    // stack it is given).
    if state == State.Stale then update()(using Observe.empty)
    cachedValue

  def get(using ctx: Observe): A =
    ctx.stack.headOption.foreach { handle =>
      subscribers += handle
      handle.addUnsubscribe(Unsubscribe(handle, subscribers))
    }

    if state == State.Stale then update()
    cachedValue
