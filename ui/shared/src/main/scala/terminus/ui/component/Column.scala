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
import terminus.ui.style.LayoutProps

import scala.collection.Seq

final class Column(
    val size: Size,
    layoutProps: LayoutProps = LayoutProps.default,
    context: DefaultEvent & DefaultLayout
) extends Component:

  def react(using React): Unit =
    context.components.foreach(_.react)

  def measure(constraint: Constraint): Dimensions =
    val children = context.components

    val naturalHeight =
      children.map(nonFlexibleHeight(_, constraint.maxWidth)).sum
    val targetHeight =
      size.height match
        case Measurement.Fixed(cells) => cells
        case Measurement.WrapContent  => naturalHeight
        case _                        =>
          constraint.maxHeight match
            case Infinity   => naturalHeight
            case bound: Int => bound
    val contentHeight =
      val lowerBounded = targetHeight.max(constraint.minHeight)
      constraint.maxHeight match
        case Infinity   => lowerBounded
        case bound: Int => lowerBounded.min(bound)

    val mainHeights =
      resolveMainAxis(children, contentHeight, constraint.maxWidth)
    val childDimensions = children.zip(mainHeights).map { (child, h) =>
      child.measure(childCrossConstraint(h, constraint.maxWidth))
    }

    val naturalWidth = childDimensions.map(_.width).maxOption.getOrElse(0)
    val targetWidth =
      size.width match
        case Measurement.Fixed(cells) => cells
        case Measurement.WrapContent  => naturalWidth
        case _                        =>
          constraint.maxWidth match
            case Infinity   => naturalWidth
            case bound: Int => bound
    val contentWidth =
      val lowerBounded = targetWidth.max(constraint.minWidth)
      constraint.maxWidth match
        case Infinity   => lowerBounded
        case bound: Int => lowerBounded.min(bound)

    constraint.constrain(Dimensions(contentWidth, contentHeight))

  def minIntrinsicWidth(height: Int | Infinity): Int =
    context.components.map(_.minIntrinsicWidth(height)).maxOption.getOrElse(0)

  def maxIntrinsicWidth(height: Int | Infinity): Int =
    context.components.map(_.maxIntrinsicWidth(height)).maxOption.getOrElse(0)

  def minIntrinsicHeight(width: Int | Infinity): Int =
    context.components.map(_.minIntrinsicHeight(width)).sum

  def maxIntrinsicHeight(width: Int | Infinity): Int =
    context.components.map(_.maxIntrinsicHeight(width)).sum

  def render(dimensions: Dimensions, buf: Buffer): Unit =
    val children = context.components
    val mainHeights =
      resolveMainAxis(children, dimensions.height, dimensions.width)
    val freeSpace = (dimensions.height - mainHeights.sum).max(0)
    val n = children.size

    val (startOffset, gap) = layoutProps.justify match
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

    var y = startOffset
    children.zip(mainHeights).foreach { (child, h) =>
      if h > 0 then
        val w = child.measure(childCrossConstraint(h, dimensions.width)).width
        val x = layoutProps.align match
          case Align.Stretch => 0
          case Align.Start   => 0
          case Align.End     => dimensions.width - w
          case Align.Center  => (dimensions.width - w) / 2
        // The child renders from its own origin into a view clipped to its
        // slot; anything past the column's bounds is discarded by the view
        // rather than spilling onto whatever sits below. `y` still advances by
        // the full slot so the remaining children stay correctly placed.
        child.render(Dimensions(w, h), buf.view(Rect(x, y, w, h)))
      y += h + gap
    }

  /** A child's contribution to the main axis before percentage/weight children
    * divide up what's left: literal cells for [[Measurement.Fixed]], an
    * intrinsic query for [[Measurement.WrapContent]],
    * [[Measurement.MaxIntrinsic]] and [[Measurement.MinIntrinsic]] (none of
    * which depend on leftover space), and nothing yet for
    * [[Measurement.Percentage]]/[[Measurement.Weight]].
    */
  private def nonFlexibleHeight(
      child: Component,
      crossExtent: Int | Infinity
  ): Int =
    child.size.height match
      case Measurement.Fixed(cells)                           => cells
      case Measurement.WrapContent | Measurement.MaxIntrinsic =>
        child.maxIntrinsicHeight(crossExtent)
      case Measurement.MinIntrinsic => child.minIntrinsicHeight(crossExtent)
      case Measurement.Percentage(_) | Measurement.Weight(_) => 0

  /** Allocate `available` cells across `children` along the main axis: fixed
    * (and intrinsically-sized) children first, then percentage children split
    * what's left, then weighted children split whatever remains after that.
    */
  private def resolveMainAxis(
      children: Seq[Component],
      available: Int,
      crossExtent: Int | Infinity
  ): IndexedSeq[Int] =
    val phase1 = children.map(nonFlexibleHeight(_, crossExtent))
    val afterFixed = (available - phase1.sum).max(0)

    // Percentage children draw from what's left after fixed/intrinsic children.
    // When their shares sum to no more than the available pool they take their
    // literal percentages, leaving the remainder for weighted children. When
    // they oversubscribe (sum > 100% of the pool) we scale them down to fit
    // exactly, so no child is clipped and none falls past the column's edge;
    // there is no remainder in that case, so weighted children get nothing.
    val rawPercentage = children.map { child =>
      child.size.height match
        case Measurement.Percentage(p) => afterFixed * p
        case _                         => 0.0
    }
    val percentageSum = rawPercentage.sum
    val percentageScale =
      if percentageSum > afterFixed then afterFixed / percentageSum else 1.0
    val phase2 = rawPercentage.map(px => (px * percentageScale).toInt)
    val afterPercentage = (afterFixed - phase2.sum).max(0)

    val totalWeight = children.map { child =>
      child.size.height match
        case Measurement.Weight(w) => w
        case _                     => 0
    }.sum

    children.indices.map { i =>
      children(i).size.height match
        case Measurement.Percentage(_) => phase2(i)
        case Measurement.Weight(w)     =>
          if totalWeight > 0 then
            (afterPercentage.toDouble * w / totalWeight).toInt
          else 0
        case _ => phase1(i)
    }

  /** The constraint offered to a child on the cross axis (width): tight to the
    * column's width under [[Align.Stretch]], otherwise loose so the child can
    * report its own natural width.
    */
  private def childCrossConstraint(
      height: Int,
      crossBound: Int | Infinity
  ): Constraint =
    layoutProps.align match
      case Align.Stretch =>
        crossBound match
          case Infinity   => Constraint(0, Infinity, height, height)
          case bound: Int => Constraint(bound, bound, height, height)
      case _ => Constraint(0, crossBound, height, height)

object Column:
  def apply(size: Size, style: LayoutProps => LayoutProps = identity)(
      body: Event & Layout ?=> Unit
  )(using ctx: Layout): Unit =
    ctx.addComponent { runtime =>
      val focusId = FocusId.next
      val context = new DefaultEvent(focusId, runtime)
        with DefaultLayout(runtime) {}
      // Evaluate body here so we do not retain a reference to it and it can be garbage collected.
      body(using context)

      new Column(size, style(LayoutProps.default), context)
    }
