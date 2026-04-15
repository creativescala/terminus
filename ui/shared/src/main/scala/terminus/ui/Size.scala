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

/** Represents the size of component in terms of temrinal cells. */
final case class Size(width: Int, height: Int) {

  /** Combine two sizes into a row. That is, add together width and take the max
    * height.
    */
  def row(that: Size): Size =
    Size(this.width + that.width, this.height.max(that.height))

  /** Combine two sizes into a column. That is, take the max of width and add
    * together the height.
    */
  def column(that: Size): Size =
    Size(this.width.max(that.width), this.height + that.height)
}
object Size {
  val zero: Size = Size(0, 0)
}
