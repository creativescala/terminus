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

// Some references on parsing terminal codes:
//   https://github.com/crossterm-rs/crossterm/blob/master/src/event/sys/unix/parse.rs
//   https://github.com/Textualize/textual/blob/main/src/textual/_ansi_sequences.py

/** A pure incremental parser for terminal key input. Each state accepts a
  * single token of input, via `parse`, and produces a [[KeyParser.Result]]: a
  * completed `Key`, `Eof`, or the next `KeyParser` state indicating more input
  * is needed.
  *
  * A driver feeds the parser input, starting from [[KeyParser.Start]], and
  * decides how to read that input. When [[isDisambiguating]] is true the parser
  * has seen a character, such as escape, that may be either a key press on its
  * own or the start of an escape sequence. The driver should then read with a
  * timeout and feed `Timeout` to the parser if no input arrives in time.
  */
enum KeyParser:
  /** Waiting for the first character of a key. */
  case Start

  /** Seen a single character that is either a key press on its own or the start
    * of an escape sequence. Waiting for further input to decide which.
    */
  case Disambiguating(first: Char, sequence: KeySequence)

  /** Partway through an escape sequence. */
  case InSequence(accum: String, sequence: KeySequence)

  /** True if the next input should be read with a timeout, because the parser
    * is waiting to decide whether a character was a key press on its own or the
    * start of an escape sequence.
    */
  def isDisambiguating: Boolean =
    this match
      case Disambiguating(_, _) => true
      case _                    => false

  /** Feed a single token of input to the parser. */
  def parse(input: Timeout | Eof | Char): KeyParser.Result =
    this match
      case Start =>
        input match
          case Eof     => Eof
          case Timeout => Start
          case c: Char =>
            KeyMappings.default.get(c) match
              case None                  => Key(c)
              case Some(k: Key)          => k
              case Some(ks: KeySequence) => Disambiguating(c, ks)

      case Disambiguating(first, sequence) =>
        input match
          case Eof     => sequence.root
          case Timeout => sequence.root
          case c: Char => KeyParser.advance(s"$first$c", sequence)

      case InSequence(accum, sequence) =>
        input match
          case Eof     => Eof
          case Timeout => Key.unknown(accum)
          case c: Char => KeyParser.advance(accum + c.toString, sequence)

object KeyParser:
  /** The result of feeding input to a `KeyParser`: end of input, a completed
    * key press, or the next parser state indicating more input is needed.
    */
  type Result = Eof | Key | KeyParser

  private def advance(accum: String, sequence: KeySequence): Result =
    if sequence.sequences.contains(accum) then sequence.sequences(accum)
    else if sequence.subSequences.contains(accum) then
      InSequence(accum, sequence)
    else Key.unknown(accum)
