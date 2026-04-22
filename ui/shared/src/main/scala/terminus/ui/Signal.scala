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

/** Read-only view of a reactive value.
  *
  * Passed to components that only need to observe a value, not change it.
  */
trait ReadSignal[A]:
  /** Read the signal's current value, registering the enclosing component as a
    * subscriber. Must be called in render scope (inside a component content
    * lambda) where a [[RenderContext]] is available.
    *
    * The [[RenderContext]] parameter is unused in the current full-frame
    * re-render implementation; it is here to enforce the render-scope
    * constraint at the type level and will drive targeted re-rendering once
    * dependency tracking is fully implemented.
    */
  def get(using RenderContext): A

  /** Read the signal's current value without registering a dependency.
    *
    * Use in event handlers and other setup-scope code where no
    * [[RenderContext]] is in scope. Does not trigger re-render on its own.
    */
  def peek: A

/** A readable and writable reactive value owned by an [[EventContext]].
  *
  * Calling [[set]] or [[update]] schedules a re-render of the UI. The signal's
  * lifetime is tied to the [[EventContext]] that created it.
  */
trait Signal[A] extends ReadSignal[A]:
  def set(a: A): Unit
  def update(f: A => A): Unit = set(f(peek))

private[ui] final class SignalImpl[A](
    private var value: A,
    private val ec: EventContextImpl
) extends Signal[A]:
  def get(using RenderContext): A = value
  def peek: A = value

  def set(a: A): Unit =
    value = a
    ec.scheduleRerender()
