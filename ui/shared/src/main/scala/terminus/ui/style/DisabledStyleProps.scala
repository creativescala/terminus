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

/** Mix-in for styling the disabled properties of components. Mirrors
  * [[FocusStyleProps]]: components fall back to their base style when no
  * disabled style is set.
  *
  * The type `A` is the type that this is mixed into.
  */
trait DisabledStyleProps[Style, A <: DisabledStyleProps[Style, A]](
    protected var disabledDefault: Style
):
  self: WithCopy[A] =>

  // We default to no styling for disabled.
  protected var disabledStyle: Option[Style] = None

  def disabled: Option[Style] = disabledStyle

  def withDisabled(style: Style): A =
    change(Some(style))

  def withoutDisabled: A =
    change(None)

  /** Update the disabled style using the given function. If there is no current
    * disabled style it operates on the default given when the mix-in is
    * constructed, on the assumption that the caller wants disabled styles to
    * exist.
    */
  def withDisabled(update: Style => Style): A =
    change(Some(update(disabledStyle.getOrElse(disabledDefault))))

  private def change(style: Option[Style]): A =
    val updated = self.copy()
    updated.disabledStyle = style
    updated
