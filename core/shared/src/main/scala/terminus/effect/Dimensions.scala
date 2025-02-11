package terminus.effect

import org.jline.terminal.Size

/** Functionalities related to the dimensions of the terminal */
trait Dimensions extends Writer {
  def getDimensions: TerminalDimensions
}

final case class TerminalDimensions(noOfColumns: Int, noOfRows: Int)

object TerminalDimensions {
  extension (size: Size) def fromJLineSize: TerminalDimensions =
    TerminalDimensions(size.getColumns, size.getRows)
}