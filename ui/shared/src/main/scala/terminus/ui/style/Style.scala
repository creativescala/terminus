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

package terminus.ui.style

import terminus.ui.capability.Availability
import terminus.ui.capability.Focus
import terminus.ui.capability.HasAvailability
import terminus.ui.capability.HasFocus

/** A function from a component's state to the properties it renders with.
  *
  * A style is a base set of properties (`Props`) plus an ordered list of rules.
  * Each rule is a predicate on the component's state (`State`) and a patch to
  * apply when the predicate holds. Applying a style to a state folds the
  * matching patches over the base in declaration order, so:
  *
  *   - a patch is relative to whatever it is applied to: a focused style that
  *     only sets bold keeps every other base property;
  *   - rules for independent states combine (a disabled and focused component
  *     receives both patches); and
  *   - on conflict, the later-declared rule wins.
  *
  * State-specific combinators such as `focused` and `disabled` are extension
  * methods constrained by the state record's capabilities (see
  * [[terminus.ui.capability.HasFocus]]), so styling a state a component doesn't
  * have is a compile error.
  */
final class Style[-State, Props] private (
    val base: Props,
    rules: Seq[Rule[State, Props]]
):

  /** Resolve the properties to render with in the given state. */
  def apply(state: State): Props =
    rules.foldLeft(base) { case (props, rule) =>
      rule(state, props)
    }

  /** Add a rule: when `pred` holds of the component's state, apply `patch`.
    * Rules apply in declaration order, so later rules win on conflict.
    */
  def when[S <: State](pred: S => Boolean)(
      patch: Props => Props
  ): Style[S, Props] =
    new Style(base, rules :+ Rule(pred, patch))

  /** Update the base properties, which apply in every state. */
  def withBase(update: Props => Props): Style[State, Props] =
    new Style(update(base), rules)

object Style:
  def apply[State, Props](base: Props): Style[State, Props] =
    new Style(base, Seq.empty)

  extension [State <: HasFocus, Props](style: Style[State, Props])
    /** Apply `patch` when the component holds the keyboard focus. */
    def focused(patch: Props => Props): Style[State, Props] =
      style.when(_.focus == Focus.Focused)(patch)

  extension [State <: HasAvailability, Props](style: Style[State, Props])
    /** Apply `patch` when the component is disabled. */
    def disabled(patch: Props => Props): Style[State, Props] =
      style.when(_.availability == Availability.Disabled)(patch)
