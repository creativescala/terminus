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

import terminus.ui.capability.Event
import terminus.ui.capability.Layout
import terminus.ui.capability.React
import terminus.ui.event.DefaultEvent
import terminus.ui.event.FocusId
import terminus.ui.layout.Buffer
import terminus.ui.layout.Component
import terminus.ui.layout.Constraint
import terminus.ui.layout.DefaultLayout
import terminus.ui.layout.Dimensions
import terminus.ui.layout.Infinity
import terminus.ui.layout.Measurement
import terminus.ui.layout.Rect
import terminus.ui.layout.Size
import terminus.ui.style.Align
import terminus.ui.style.Justify
import terminus.ui.style.LayoutStyle

import scala.collection.Seq

final class Row(
    val size: Size,
    layoutStyle: LayoutStyle = LayoutStyle.default,
    context: DefaultEvent & DefaultLayout
) extends Component:

  def react(using React): Unit =
    context.components.foreach(_.react)

  def measure(constraint: Constraint): Dimensions =
    val children = context.components

    val naturalWidth =
      children.map(nonFlexibleWidth(_, constraint.maxHeight)).sum
    val targetWidth =
      size.width match
        case Measurement.Fixed(cells) => cells
        case Measurement.WrapContent  => naturalWidth
        case _ =>
          constraint.maxWidth match
            case Infinity   => naturalWidth
            case bound: Int => bound
    val contentWidth =
      val lowerBounded = targetWidth.max(constraint.minWidth)
      constraint.maxWidth match
        case Infinity   => lowerBounded
        case bound: Int => lowerBounded.min(bound)

    val mainWidths =
      resolveMainAxis(children, contentWidth, constraint.maxHeight)
    val childDimensions = children.zip(mainWidths).map { (child, w) =>
      child.measure(childCrossConstraint(w, constraint.maxHeight))
    }

    val naturalHeight = childDimensions.map(_.height).maxOption.getOrElse(0)
    val targetHeight =
      size.height match
        case Measurement.Fixed(cells) => cells
        case Measurement.WrapContent  => naturalHeight
        case _ =>
          constraint.maxHeight match
            case Infinity   => naturalHeight
            case bound: Int => bound
    val contentHeight =
      val lowerBounded = targetHeight.max(constraint.minHeight)
      constraint.maxHeight match
        case Infinity   => lowerBounded
        case bound: Int => lowerBounded.min(bound)

    constraint.constrain(Dimensions(contentWidth, contentHeight))

  def minIntrinsicWidth(height: Int | Infinity): Int =
    context.components.map(_.minIntrinsicWidth(height)).sum

  def maxIntrinsicWidth(height: Int | Infinity): Int =
    context.components.map(_.maxIntrinsicWidth(height)).sum

  def minIntrinsicHeight(width: Int | Infinity): Int =
    context.components.map(_.minIntrinsicHeight(width)).maxOption.getOrElse(0)

  def maxIntrinsicHeight(width: Int | Infinity): Int =
    context.components.map(_.maxIntrinsicHeight(width)).maxOption.getOrElse(0)

  def render(bounds: Rect, buf: Buffer): Unit =
    val children = context.components
    val mainWidths = resolveMainAxis(children, bounds.width, bounds.height)
    val freeSpace = (bounds.width - mainWidths.sum).max(0)
    val n = children.size

    val (startOffset, gap) = layoutStyle.justify match
      case Justify.Start        => (0, 0)
      case Justify.End          => (freeSpace, 0)
      case Justify.Center       => (freeSpace / 2, 0)
      case Justify.SpaceBetween => (0, if n > 1 then freeSpace / (n - 1) else 0)
      case Justify.SpaceAround  =>
        val space = if n > 0 then freeSpace / n else 0
        (space / 2, space)
      case Justify.SpaceEvenly =>
        val space = if n > 0 then freeSpace / (n + 1) else 0
        (space, space)

    var x = bounds.x + startOffset
    children.zip(mainWidths).foreach { (child, w) =>
      val h = child.measure(childCrossConstraint(w, bounds.height)).height
      val y = layoutStyle.align match
        case Align.Stretch => bounds.y
        case Align.Start   => bounds.y
        case Align.End     => bounds.y + bounds.height - h
        case Align.Center  => bounds.y + (bounds.height - h) / 2
      child.render(Rect(x, y, w, h), buf)
      x += w + gap
    }

  /** A child's contribution to the main axis before percentage/weight
    * children divide up what's left: literal cells for [[Measurement.Fixed]],
    * an intrinsic query for [[Measurement.WrapContent]],
    * [[Measurement.MaxIntrinsic]] and [[Measurement.MinIntrinsic]] (none of
    * which depend on leftover space), and nothing yet for
    * [[Measurement.Percentage]]/[[Measurement.Weight]].
    */
  private def nonFlexibleWidth(
      child: Component,
      crossExtent: Int | Infinity
  ): Int =
    child.size.width match
      case Measurement.Fixed(cells) => cells
      case Measurement.WrapContent | Measurement.MaxIntrinsic =>
        child.maxIntrinsicWidth(crossExtent)
      case Measurement.MinIntrinsic => child.minIntrinsicWidth(crossExtent)
      case Measurement.Percentage(_) | Measurement.Weight(_) => 0

  /** Allocate `available` cells across `children` along the main axis: fixed
    * (and intrinsically-sized) children first, then percentage children
    * split what's left, then weighted children split whatever remains after
    * that.
    */
  private def resolveMainAxis(
      children: Seq[Component],
      available: Int,
      crossExtent: Int | Infinity
  ): IndexedSeq[Int] =
    val phase1 = children.map(nonFlexibleWidth(_, crossExtent))
    val afterFixed = (available - phase1.sum).max(0)

    val phase2 = children.map { child =>
      child.size.width match
        case Measurement.Percentage(p) => (afterFixed * p).toInt
        case _                         => 0
    }
    val afterPercentage = (afterFixed - phase2.sum).max(0)

    val totalWeight = children.map { child =>
      child.size.width match
        case Measurement.Weight(w) => w
        case _                     => 0
    }.sum

    children.indices.map { i =>
      children(i).size.width match
        case Measurement.Percentage(_) => phase2(i)
        case Measurement.Weight(w) =>
          if totalWeight > 0 then
            (afterPercentage.toDouble * w / totalWeight).toInt
          else 0
        case _ => phase1(i)
    }

  /** The constraint offered to a child on the cross axis (height): tight to
    * the row's height under [[Align.Stretch]], otherwise loose so the child
    * can report its own natural height.
    */
  private def childCrossConstraint(
      width: Int,
      crossBound: Int | Infinity
  ): Constraint =
    layoutStyle.align match
      case Align.Stretch =>
        crossBound match
          case Infinity   => Constraint(width, width, 0, Infinity)
          case bound: Int => Constraint(width, width, bound, bound)
      case _ => Constraint(width, width, 0, crossBound)

object Row:
  def apply(
      size: Size,
      style: LayoutStyle => LayoutStyle = identity
  )(body: Event & Layout ?=> Unit)(using
      ctx: Layout
  ): Unit =
    ctx.addComponent { runtime =>
      val focusId = FocusId.next
      val context = new DefaultEvent(focusId, runtime)
        with DefaultLayout(runtime) {}
      // Evaluate body here so we do not retain a reference to it and it can be garbage collected.
      body(using context)

      new Row(size, style(LayoutStyle.default), context)
    }
