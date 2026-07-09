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

final class Row(
    val size: Size,
    layoutProps: LayoutProps = LayoutProps.default,
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
        case _                        =>
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
    // Children are measured loose on the cross axis so a WrapContent row
    // sizes itself to its tallest child's natural height (fit-content). Under
    // Align.Stretch children are heightened at render time against the row's
    // *resolved* height; stretching here, against the incoming bound, would
    // inflate the natural height and make wrap-content impossible.
    val childDimensions = children.zip(mainWidths).map { (child, w) =>
      child.measure(Constraint(w, w, 0, constraint.maxHeight))
    }

    val naturalHeight = childDimensions.map(_.height).maxOption.getOrElse(0)
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

    constraint.constrain(Dimensions(contentWidth, contentHeight))

  def minIntrinsicWidth(height: Int | Infinity): Int =
    context.components.map(_.minIntrinsicWidth(height)).sum

  def maxIntrinsicWidth(height: Int | Infinity): Int =
    context.components.map(_.maxIntrinsicWidth(height)).sum

  def minIntrinsicHeight(width: Int | Infinity): Int =
    context.components.map(_.minIntrinsicHeight(width)).maxOption.getOrElse(0)

  def maxIntrinsicHeight(width: Int | Infinity): Int =
    context.components.map(_.maxIntrinsicHeight(width)).maxOption.getOrElse(0)

  def render(dimensions: Dimensions, buf: Buffer): Unit =
    val children = context.components
    val mainWidths =
      resolveMainAxis(children, dimensions.width, dimensions.height)
    val freeSpace = (dimensions.width - mainWidths.sum).max(0)
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

    var x = startOffset
    children.zip(mainWidths).foreach { (child, w) =>
      if w > 0 then
        val h =
          child.measure(renderCrossConstraint(w, dimensions.height)).height
        val y = layoutProps.align match
          case Align.Stretch => 0
          case Align.Start   => 0
          case Align.End     => dimensions.height - h
          case Align.Center  => (dimensions.height - h) / 2
        // The child renders from its own origin into a view clipped to its
        // slot; anything past the row's bounds is discarded by the view rather
        // than spilling onto whatever sits beside it. `x` still advances by the
        // full slot so the remaining children stay correctly placed.
        child.render(Dimensions(w, h), buf.view(Rect(x, y, w, h)))
      x += w + gap
    }

  /** A child's contribution to the main axis before percentage/weight children
    * divide up what's left: literal cells for [[Measurement.Fixed]], an
    * intrinsic query for [[Measurement.WrapContent]],
    * [[Measurement.MaxIntrinsic]] and [[Measurement.MinIntrinsic]] (none of
    * which depend on leftover space), and nothing yet for
    * [[Measurement.Percentage]]/[[Measurement.Weight]].
    */
  private def nonFlexibleWidth(
      child: Component,
      crossExtent: Int | Infinity
  ): Int =
    child.size.width match
      case Measurement.Fixed(cells)                           => cells
      case Measurement.WrapContent | Measurement.MaxIntrinsic =>
        child.maxIntrinsicWidth(crossExtent)
      case Measurement.MinIntrinsic => child.minIntrinsicWidth(crossExtent)
      case Measurement.Percentage(_) | Measurement.Weight(_) => 0

  /** Allocate `available` cells across `children` along the main axis in three
    * phases against a shrinking pool: fixed (and intrinsically-sized) children
    * take their natural widths first; then percentage children each take their
    * fraction of what's left (an absolute share of that pool, so they may leave
    * a gap, and are scaled down to fit if they oversubscribe it); then weighted
    * children split whatever remains after that as relative shares. Percentages
    * are thus fractions of the *remaining* space, not of `available` — see
    * [[Measurement.Percentage]].
    */
  private def resolveMainAxis(
      children: Seq[Component],
      available: Int,
      crossExtent: Int | Infinity
  ): IndexedSeq[Int] =
    val phase1 = children.map(nonFlexibleWidth(_, crossExtent))
    val afterFixed = (available - phase1.sum).max(0)

    // Percentage children draw from what's left after fixed/intrinsic children.
    // When their shares sum to no more than the available pool they take their
    // literal percentages, leaving the remainder for weighted children. When
    // they oversubscribe (sum > 100% of the pool) we scale them down to fit
    // exactly, so no child is clipped and none falls off the row's edge; there
    // is no remainder in that case, so weighted children get nothing.
    val rawPercentage = children.map { child =>
      child.size.width match
        case Measurement.Percentage(p) => afterFixed * p
        case _                         => 0.0
    }
    val percentageSum = rawPercentage.sum
    val percentageScale =
      if percentageSum > afterFixed then afterFixed / percentageSum else 1.0
    val phase2 = rawPercentage.map(px => (px * percentageScale).toInt)
    val afterPercentage = (afterFixed - phase2.sum).max(0)

    val totalWeight = children.map { child =>
      child.size.width match
        case Measurement.Weight(w) => w
        case _                     => 0
    }.sum

    children.indices.map { i =>
      children(i).size.width match
        case Measurement.Percentage(_) => phase2(i)
        case Measurement.Weight(w)     =>
          if totalWeight > 0 then
            (afterPercentage.toDouble * w / totalWeight).toInt
          else 0
        case _ => phase1(i)
    }

  /** The constraint offered to a child on the cross axis (height) at render
    * time: tight to the row's resolved height under [[Align.Stretch]], so
    * children stretch to match the row, otherwise loose so the child takes its
    * natural height. During measurement children are always offered a loose
    * cross constraint — see [[measure]].
    */
  private def renderCrossConstraint(
      width: Int,
      crossBound: Int | Infinity
  ): Constraint =
    layoutProps.align match
      case Align.Stretch =>
        crossBound match
          case Infinity   => Constraint(width, width, 0, Infinity)
          case bound: Int => Constraint(width, width, bound, bound)
      case _ => Constraint(width, width, 0, crossBound)

object Row:
  def apply(
      size: Size,
      style: LayoutProps => LayoutProps = identity
  )(body: Event & Layout ?=> Unit)(using
      ctx: Layout
  ): Unit =
    ctx.addComponent { runtime =>
      val focusId = FocusId.next
      val context = new DefaultEvent(focusId, runtime)
        with DefaultLayout(runtime) {}
      // Evaluate body here so we do not retain a reference to it and it can be garbage collected.
      body(using context)

      new Row(size, style(LayoutProps.default), context)
    }
