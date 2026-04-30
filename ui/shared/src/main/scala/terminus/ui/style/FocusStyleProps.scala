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

/** Mix-in for styling the focus properties of components.
  *
  * The type `A` is the type that `FocusStyle` is mixed into.
  */
trait FocusStyleProps[Style, A <: FocusStyleProps[Style, A]](
    protected var focusDefault: Style
):
  self: WithCopy[A] =>

  // We default to no styling for focus.
  protected var focusStyle: Option[Style] = None

  def focus: Option[Style] = focusStyle

  def withFocus(style: Style): A =
    change(Some(style))

  def withoutFocus: A =
    change(None)

  /** Update the focus style using the given function. If this method is called
    * and there is no current focus style (i.e. focus is None) it will operate
    * on the default focus style given when the mix-in is constructed. The
    * assumption is that when the user calls this method they want focus styles
    * to exist.
    */
  def withFocus(update: Style => Style): A =
    change(Some(update(focusStyle.getOrElse(focusDefault))))

  private def change(style: Option[Style]): A =
    val updated = self.copy()
    updated.focusStyle = style
    updated
