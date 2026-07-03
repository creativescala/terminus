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

/** The capability of responding to the user committing to a particular value.
  * This could be clicking a button, choosing an item from a list, or submitting
  * some free-form text. Submit is parameterized by the type of value that can
  * be submitted.
  */
trait Submit[A]:
  def onSubmit(handler: A => Unit): Unit

object Submit:
  extension (submit: Submit[Unit])
    /** Register a handler for components that submit no value, such as
      * [[terminus.ui.component.Button]], avoiding the noise of an ignored
      * parameter: `onSubmit { save() }` rather than `onSubmit(_ => save())`.
      */
    def onSubmit(handler: => Unit): Unit =
      submit.onSubmit(_ => handler)
