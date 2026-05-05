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

package terminus.ui.component

import terminus.ui.AppContent
import terminus.ui.AppContext
import terminus.ui.Buffer
import terminus.ui.ChildContext
import terminus.ui.Component
import terminus.ui.Dimensions
import terminus.ui.Rect
import terminus.ui.Size

import scala.collection.mutable

class Column() extends ChildContext, Component:
  private val children: mutable.ArrayBuffer[Component] =
    mutable.ArrayBuffer.empty

  private def availableSize: Dimensions =
    children.foldLeft(Dimensions.zero)((acc, c) =>
      acc.column(c.size.toDimensions)
    )

  def size: Size =
    val d = availableSize
    Size.fixed(d.width, d.height)

  def add(component: Component): Unit =
    children += component

  def render(bounds: Rect, buf: Buffer): Unit =
    var y = bounds.y
    children.foreach { child =>
      val childSize = child.size.toDimensions
      child.render(Rect(bounds.x, y, childSize.width, childSize.height), buf)
      y += childSize.height
    }

object Column:
  def apply[A](
      f: AppContent[A]
  )(using parent: AppContext): A =
    val column = new Column()
    given AppContext = AppContext.child(parent, column)
    val result = f
    parent.add(column)
    result
