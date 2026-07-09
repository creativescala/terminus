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

/** A Rule is a predicate on the component's state (`State`) and a patch to
  * apply when the predicate holds.
  */
final class Rule[-State, Props](pred: State => Boolean, patch: Props => Props):
  def apply(state: State, props: Props): Props =
    if pred(state) then patch(props) else props
