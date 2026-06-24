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

import terminus.ui.capability.Layout
import terminus.ui.runtime.Runtime

import scala.collection.mutable

/** The default implementation of [[terminus.ui.capability.Layout]]. */
trait DefaultLayout(runtime: Runtime) extends Layout:
  private[ui] val components: mutable.ArrayBuffer[Component] =
    mutable.ArrayBuffer.empty

  def addComponent(build: Runtime => Component): Unit =
    components += build(runtime)
object DefaultLayout:
  def apply(runtime: Runtime): DefaultLayout =
    new DefaultLayout(runtime) {}
