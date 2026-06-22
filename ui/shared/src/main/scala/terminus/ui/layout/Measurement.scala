package terminus.ui.layout

/** A Measurement expresses a dimension of size in terms of a fixed number of
  * cells, a portion of the parent's space, or in terms of the space occupied by
  * children.
  */
enum Measurement:
  /** Exactly `cells` */
  case Fixed(cells: Int)

  /** A fraction of the parent container's size on this axis (0.0–1.0). */
  case Percentage(percent: Double)

  /** A proportional share of the parent's remaining space after fixed and
    * percentage children have been placed.
    */
  case Weight(weight: Int)

  /** Exactly match the aggregate size of children elements. */
  case WrapContent

  /** Match childrens' natural maximum size limits. */
  case MaxIntrinsic

  /** Match childrens' natural minimum size limits. */
  case MinIntrinsic
