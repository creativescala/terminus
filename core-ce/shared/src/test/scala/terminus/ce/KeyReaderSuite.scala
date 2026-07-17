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

package terminus.ce

import cats.effect.IO
import cats.effect.std.Queue
import cats.effect.unsafe.implicits.global
import munit.FunSuite
import terminus.Eof
import terminus.Key

import scala.concurrent.duration.*

class KeyReaderSuite extends FunSuite:
  /** A char source backed by a queue preloaded with `input`; reads beyond the
    * end block, as reads from a quiet terminal do.
    */
  def charsOf(input: (Eof | Char)*): IO[IO[Eof | Char]] =
    for
      queue <- Queue.unbounded[IO, Eof | Char]
      _ <- input.toList.foldLeft(IO.unit)((io, c) => io >> queue.offer(c))
    yield queue.take

  def run[A](io: IO[A]): A = io.timeout(5.seconds).unsafeRunSync()

  test("A plain character is read as a key press") {
    val io = charsOf('a').flatMap(chars => KeyReader.readKey(chars))
    assertEquals(run(io), Key('a'): Eof | Key)
  }

  test("A complete escape sequence is read as the mapped key press") {
    val io =
      charsOf('\u001b', '[', 'A').flatMap(chars => KeyReader.readKey(chars))
    assertEquals(run(io), Key.up: Eof | Key)
  }

  test("Escape with no further input is the escape key, after the timeout") {
    val io = charsOf('\u001b').flatMap(chars =>
      KeyReader.readKey(chars, timeout = 20.millis)
    )
    assertEquals(run(io), Key.escape: Eof | Key)
  }

  test("A character arriving within the timeout is not lost") {
    val io =
      for
        queue <- Queue.unbounded[IO, Eof | Char]
        _ <- queue.offer('\u001b')
        offer <- (IO.sleep(20.millis) >> queue.offer('[') >> queue.offer(
          'A'
        )).start
        key <- KeyReader.readKey(queue.take, timeout = 5.seconds)
        _ <- offer.join
      yield key
    assertEquals(run(io), Key.up: Eof | Key)
  }

  test("Eof is passed through") {
    val io = charsOf(Eof).flatMap(chars => KeyReader.readKey(chars))
    assertEquals(run(io), Eof: Eof | Key)
  }

  test("Successive reads parse successive keys from one source") {
    val io = charsOf('\u001b', '[', 'A', 'b').flatMap { chars =>
      for
        first <- KeyReader.readKey(chars)
        second <- KeyReader.readKey(chars)
      yield (first, second)
    }
    assertEquals(run(io), (Key.up: Eof | Key, Key('b'): Eof | Key))
  }
