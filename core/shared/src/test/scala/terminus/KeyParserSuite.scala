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

package terminus

import munit.FunSuite

class KeyParserSuite extends FunSuite:
  /** Feed the characters of `input` to the parser, asserting it asks for more
    * input until the final character, which must produce `expected`.
    */
  def assertParses(input: String, expected: Key): Unit =
    val result = input.foldLeft[KeyParser.Result](KeyParser.Start) {
      (state, char) =>
        state match
          case parser: KeyParser => parser.parse(char)
          case other => fail(s"Parsing $input finished early with $other")
    }
    assertEquals(result, expected: KeyParser.Result, s"Input is $input")

  /** Feed the characters of `input` to the parser, asserting it asks for more
    * input after every character, and return the resulting parser state.
    */
  def parserAfter(input: String): KeyParser =
    input.foldLeft(KeyParser.Start) { (parser, char) =>
      parser.parse(char) match
        case next: KeyParser => next
        case other => fail(s"Parsing $input finished early with $other")
    }

  test("A plain character is emitted as a key press") {
    assertEquals(KeyParser.Start.parse('a'), Key('a'): KeyParser.Result)
  }

  test("A control character is emitted as the mapped key press") {
    assertEquals(
      KeyParser.Start.parse('\u0003'),
      Key.controlC: KeyParser.Result
    )
  }

  test("Eof at the start is emitted as Eof") {
    assertEquals(KeyParser.Start.parse(Eof), Eof: KeyParser.Result)
  }

  test("Timeout at the start needs more input") {
    assertEquals(
      KeyParser.Start.parse(Timeout),
      KeyParser.Start: KeyParser.Result
    )
  }

  test("Escape followed by a timeout is emitted as the escape key") {
    assertEquals(
      parserAfter("\u001b").parse(Timeout),
      Key.escape: KeyParser.Result
    )
  }

  test("Escape followed by Eof is emitted as the escape key") {
    assertEquals(parserAfter("\u001b").parse(Eof), Key.escape: KeyParser.Result)
  }

  test("Only disambiguation after an escape reads with a timeout") {
    assert(!KeyParser.Start.isDisambiguating)
    assert(parserAfter("\u001b").isDisambiguating)
    assert(!parserAfter("\u001b[").isDisambiguating)
    assert(!parserAfter("\u001b[1;5").isDisambiguating)
  }

  test("A complete escape sequence is emitted as the mapped key press") {
    assertParses("\u001b[A", Key.up)
    assertParses("\u001bOP", Key.f1)
    assertParses("\u001b[1;5A", Key.controlUp)
    assertParses("\u001b[24;6~", Key.controlF24)
    assertParses("\u001bb", Key.controlLeft)
  }

  test("An unrecognized escape sequence is emitted as an unknown key press") {
    assertParses("\u001bq", Key.unknown("\u001bq"))
    // The unknown key is emitted as soon as the input is neither a complete
    // sequence nor a prefix of one, so a trailing character (the Z in
    // ESC[1;9Z) is never consumed.
    assertParses("\u001b[1;9", Key.unknown("\u001b[1;9"))
  }

  test("Eof within an escape sequence is emitted as Eof") {
    assertEquals(parserAfter("\u001b[").parse(Eof), Eof: KeyParser.Result)
  }

  test("Timeout within an escape sequence is emitted as an unknown key press") {
    assertEquals(
      parserAfter("\u001b[").parse(Timeout),
      Key.unknown("\u001b["): KeyParser.Result
    )
  }
