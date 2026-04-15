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

/** The position and size of a component within the terminal grid.
  *
  * Coordinates are 0-based, with (0, 0) at the top-left. x increases to the
  * right, y increases downward. Conversion to 1-based terminal coordinates
  * happens only at flush time inside [[Buffer.render]].
  */
final case class Rect(x: Int, y: Int, width: Int, height: Int):
  def size: Size = Size(width, height)

  /** Exclusive right edge. */
  def right: Int = x + width

  /** Exclusive bottom edge. */
  def bottom: Int = y + height
