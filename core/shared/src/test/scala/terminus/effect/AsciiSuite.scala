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

package terminus.effect

import munit.FunSuite

class AsciiSuite extends FunSuite {
  import Ascii.*

  test("String escape characters match their Ascii characters") {
    assertEquals(HT, '\t')
    assertEquals(LF, '\n')
    assertEquals(CR, '\r')
  }

  test("Ascii control characters are identified correctly") {
    assertEquals(NUL.isControlChar, true)
    assertEquals(SOH.isControlChar, true)
    assertEquals(STX.isControlChar, true)
    assertEquals(ETX.isControlChar, true)
    assertEquals(EOT.isControlChar, true)
    assertEquals(ENQ.isControlChar, true)
    assertEquals(ACK.isControlChar, true)
    assertEquals(BEL.isControlChar, true)
    assertEquals(BS.isControlChar, true)
    assertEquals(HT.isControlChar, true)
    assertEquals(LF.isControlChar, true)
    assertEquals(VT.isControlChar, true)
    assertEquals(FF.isControlChar, true)
    assertEquals(CR.isControlChar, true)
    assertEquals(SO.isControlChar, true)
    assertEquals(SI.isControlChar, true)
    assertEquals(DLE.isControlChar, true)
    assertEquals(DC1.isControlChar, true)
    assertEquals(DC2.isControlChar, true)
    assertEquals(DC3.isControlChar, true)
    assertEquals(DC4.isControlChar, true)
    assertEquals(NAK.isControlChar, true)
    assertEquals(SYN.isControlChar, true)
    assertEquals(ETB.isControlChar, true)
    assertEquals(CAN.isControlChar, true)
    assertEquals(EM.isControlChar, true)
    assertEquals(SUB.isControlChar, true)
    assertEquals(ESC.isControlChar, true)
    assertEquals(FS.isControlChar, true)
    assertEquals(GS.isControlChar, true)
    assertEquals(RS.isControlChar, true)
    assertEquals(US.isControlChar, true)
  }

  test("Non-control characters are not identified as control characters") {
    assertEquals(DEL.isControlChar, false)
    assertEquals('a'.isControlChar, false)
    assertEquals('A'.isControlChar, false)
    assertEquals('1'.isControlChar, false)
    assertEquals('@'.isControlChar, false)
    assertEquals('Þ'.isControlChar, false)
    assertEquals('な'.isControlChar, false)
    assertEquals('私'.isControlChar, false)
  }

  test("Regular characters are identified as printable") {
    assertEquals('a'.isPrintableChar, true)
    assertEquals('A'.isPrintableChar, true)
    assertEquals('1'.isPrintableChar, true)
    assertEquals('@'.isPrintableChar, true)
    assertEquals('Þ'.isPrintableChar, true)
    assertEquals('な'.isPrintableChar, true)
    assertEquals('私'.isPrintableChar, true)
  }

  test("Control characters and DEL are not identified as printable") {
    assertEquals(NUL.isPrintableChar, false)
    assertEquals(SOH.isPrintableChar, false)
    assertEquals(STX.isPrintableChar, false)
    assertEquals(ETX.isPrintableChar, false)
    assertEquals(EOT.isPrintableChar, false)
    assertEquals(ENQ.isPrintableChar, false)
    assertEquals(ACK.isPrintableChar, false)
    assertEquals(BEL.isPrintableChar, false)
    assertEquals(BS.isPrintableChar, false)
    assertEquals(HT.isPrintableChar, false)
    assertEquals(LF.isPrintableChar, false)
    assertEquals(VT.isPrintableChar, false)
    assertEquals(FF.isPrintableChar, false)
    assertEquals(CR.isPrintableChar, false)
    assertEquals(SO.isPrintableChar, false)
    assertEquals(SI.isPrintableChar, false)
    assertEquals(DLE.isPrintableChar, false)
    assertEquals(DC1.isPrintableChar, false)
    assertEquals(DC2.isPrintableChar, false)
    assertEquals(DC3.isPrintableChar, false)
    assertEquals(DC4.isPrintableChar, false)
    assertEquals(NAK.isPrintableChar, false)
    assertEquals(SYN.isPrintableChar, false)
    assertEquals(ETB.isPrintableChar, false)
    assertEquals(CAN.isPrintableChar, false)
    assertEquals(EM.isPrintableChar, false)
    assertEquals(SUB.isPrintableChar, false)
    assertEquals(ESC.isPrintableChar, false)
    assertEquals(FS.isPrintableChar, false)
    assertEquals(GS.isPrintableChar, false)
    assertEquals(RS.isPrintableChar, false)
    assertEquals(US.isPrintableChar, false)
    assertEquals(DEL.isControlChar, false)
  }
}
