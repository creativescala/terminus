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

package terminus.ui.capability

import terminus.ui.react.Listener

import scala.annotation.implicitNotFound
import scala.collection.mutable

/** The capability to observe signals.
  *
  * Reading a signal with `get` requires an Observe, and subscribes the
  * enclosing tracked computation to the signal: when the signal changes, the
  * computation re-runs. The read is the subscription — there is no separate
  * dependency-declaration step.
  *
  * An Observe is in scope only inside tracked computations: a render pass
  * (measure, the intrinsics, and render) or the thunk of a computed signal or
  * an effect. It is deliberately unavailable in setup and event-handler code,
  * where reads should use `peek`.
  */
@implicitNotFound(
  """signal.get requires an Observe capability, which is only available inside
a tracked computation: a component's measure or render, or the thunk of a
computed signal or effect.

If you meant to read this signal reactively, move the read inside a
component's content or a computed: Text(size) { computed { mySignal.get } }

If you intentionally want an untracked read, in setup or event-handler code,
use signal.peek instead."""
)
trait Observe:
  /** The tracked computations currently running, innermost last. The head is
    * what a `get` subscribes to the signal being read.
    */
  private[ui] def stack: mutable.Stack[Listener]

object Observe:
  /** An Observe with no enclosing computation: reads succeed but subscribe
    * nothing. Used by the framework for untracked evaluation.
    */
  private[ui] def empty: Observe = new Observe:
    val stack: mutable.Stack[Listener] = mutable.Stack.empty
