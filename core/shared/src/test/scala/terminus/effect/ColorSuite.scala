package terminus.effect

import munit.FunSuite
import terminus.StringBuilderTerminal

class ColorSuite extends FunSuite {
  test("Foreground color code reverts to enclosing color after leaving inner colored block") {
    val result =
      StringBuilderTerminal.run { t ?=>
        t.foreground.blue{
          t.write("Blue ")
          t.foreground.red{ t.write("Red ") }
          t.write("Blue ")
        }
      }

    assertEquals(
      result,
      s"${AnsiCodes.foreground.blue}Blue ${AnsiCodes.foreground.red}Red ${AnsiCodes.foreground.blue}Blue ${AnsiCodes.foreground.default}"
    )
  }
}
