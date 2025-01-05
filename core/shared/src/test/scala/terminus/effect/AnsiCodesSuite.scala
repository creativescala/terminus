package terminus.effect

import munit.FunSuite

class AnsiCodesSuite extends FunSuite {
  test("esc is the correct character") {
    assertEquals(AnsiCodes.esc, '\u001b')
  }

  test("csi is the correct code") {
    assertEquals(AnsiCodes.csiCode, "[")
  }

  test("csi strings with no arguments are correctly constructed") {
    assertEquals(AnsiCodes.csi("A"), "[A")
  }

  test("csi strings with a single argument are correctly constructed") {
    assertEquals(AnsiCodes.csi("m", "49"), "[49m")
  }

  test("csi strings with multiple arguments are correctly constructed") {
    assertEquals(AnsiCodes.csi("m", "4", "1"), "[4;1m")
  }
}
