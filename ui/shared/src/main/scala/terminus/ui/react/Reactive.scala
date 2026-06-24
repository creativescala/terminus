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

import terminus.ui.capability.React

import scala.collection.mutable
import scala.compiletime.uninitialized

/** A mutable value that automatically updates dependencies when it changes. */
sealed trait Reactive[A]:
  private[ui] def state: State

  /** Read the reactive's current value without registering a dependency or
    * recomputing.
    */
  def peek: A

  /** Read the Var's current value, registering the enclosing reactive context
    * as a subscriber and recomputing if necessary.
    */
  def get(using ctx: React): A
object Reactive:
  def apply[A](thunk: React ?=> A): Reactive[A] = Computed(thunk)

final class Var[A](private var currentValue: A) extends Reactive[A]:
  val subscribers: mutable.Set[Listener] = mutable.Set.empty

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

  def get(using ctx: React): A =
    ctx.stack.headOption.foreach { handle =>
      subscribers += handle
      handle.addUnsubscribe(Unsubscribe(handle, subscribers))
    }
    currentValue
object Var:
  def apply[A](initial: A): Var[A] = new Var(initial)

final class Computed[A](thunk: React ?=> A) extends Reactive[A], Listener:
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

  protected def update()(using ctx: React): Unit =
    // Clear sources. They will be recomputed as parents run.
    sources.foreach(_.apply())
    sources.clear()

    ctx.stack.push(this)

    cachedValue = thunk(using ctx)
    state = State.Fresh
    ctx.stack.pop()
    ()

  def peek: A =
    cachedValue

  def get(using ctx: React): A =
    ctx.stack.headOption.foreach { handle =>
      subscribers += handle
      handle.addUnsubscribe(Unsubscribe(handle, subscribers))
    }

    if state == State.Stale then update()
    cachedValue
