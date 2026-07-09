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

package terminus.ui.layout

/** The layout size of a component: a measurement for each axis.
  *
  * For components with a known fixed size use [[Size.fixed]]. Other
  * measurements are resolved by the layout algorithm at render time.
  */
final case class Size(width: Measurement, height: Measurement)
object Size:
  def fixed(width: Int, height: Int): Size =
    Size(Measurement.Fixed(width), Measurement.Fixed(height))

  val zero: Size = fixed(0, 0)

  /** Wrap content in both dimensions. */
  val wrapContent: Size =
    Size(Measurement.WrapContent, Measurement.WrapContent)
