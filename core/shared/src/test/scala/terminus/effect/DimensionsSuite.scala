package terminus.effect

import munit.FunSuite
import terminus.{JLineTerminal, Terminal}

import scala.io.StdIn.readLine

class DimensionsSuite extends FunSuite {
  test("Should get dimensions of the current terminal size") {

    JLineTerminal.run {
      JLineTerminal.dimensions.getDimensions
      JLineTerminal.flush()
    }

    assertEquals(readLine(), "work plz")
  }
}
