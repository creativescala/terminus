package terminus.effect

import terminus.Eof
import scala.concurrent.duration.Duration
import terminus.Timeout

/** A Reader that reads input from the given String. Mostly useful for testing.
  */
class StringBufferReader(input: String)
    extends Reader,
      NonBlockingReader,
      KeyReader,
      TerminalKeyReader {
  private var index: Int = 0

  def read(): Eof | Char =
    if index >= input.size then Eof
    else {
      val char = input(index)
      index = index + 1
      char
    }

  def read(duration: Duration): Timeout | Eof | Char =
    read()
}
