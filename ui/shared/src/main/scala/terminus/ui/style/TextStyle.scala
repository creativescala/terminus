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

import terminus.ui.capability.ComponentState

/** Styling for text-like components ([[terminus.ui.component.Text]],
  * [[terminus.ui.component.Line]], [[terminus.ui.component.Button]],
  * [[terminus.ui.component.Select]], and [[terminus.ui.component.TextInput]]):
  * a [[Style]] selecting [[TextProps]] from the component's
  * [[terminus.ui.capability.ComponentState]].
  */
type TextStyle = Style[ComponentState, TextProps]

object TextStyle:
  val default: TextStyle = Style(TextProps.default)
