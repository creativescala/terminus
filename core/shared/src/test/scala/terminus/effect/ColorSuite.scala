package terminus.effect

import munit.FunSuite
import terminus.StringBuilderTerminal

class ColorSuite extends FunSuite {
  test("Foreground color code reverts to enclosing color after leaving inner colored block") {
    val result =
      StringBuilderTerminal.run { t ?=>
        t.foreground.blue{
          t.write("Blue 1")
          t.foreground.red{ t.write("Red") }
          t.write("Blue 2")
        }
      }

    assertEquals(
      result,
      s"${AnsiCodes.foreground.blue}Blue 1${AnsiCodes.foreground.red}Red${AnsiCodes.foreground.blue}Blue 2${AnsiCodes.foreground.default}"
    )
  }
}
