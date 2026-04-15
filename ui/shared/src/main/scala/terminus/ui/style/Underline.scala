package terminus.ui.style

/** The underline style for a terminal cell.
  *
  * [[Straight]] and [[None]] are universally supported. [[Double]], [[Curly]],
  * [[Dotted]], and [[Dashed]] follow the Kitty extension and may not be
  * supported by all terminals.
  */
enum Underline:
  case None
  case Straight
  case Double
  case Curly
  case Dotted
  case Dashed
