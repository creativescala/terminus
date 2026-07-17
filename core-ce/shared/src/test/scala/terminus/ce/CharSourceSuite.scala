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
import cats.effect.Ref
import cats.effect.unsafe.implicits.global
import munit.FunSuite
import terminus.Eof
import terminus.Timeout

import scala.concurrent.duration.*

class CharSourceSuite extends FunSuite:
  /** A read action that works through `script`, then times out forever. */
  def scripted(
      script: List[Timeout | Eof | Char]
  ): IO[IO[Timeout | Eof | Char]] =
    Ref
      .of[IO, List[Timeout | Eof | Char]](script)
      .map(remaining =>
        remaining.modify {
          case head :: tail => (tail, head)
          case Nil          => (Nil, Timeout: Timeout | Eof | Char)
        }
      )

  def run[A](io: IO[A]): A = io.timeout(5.seconds).unsafeRunSync()

  test("The pump delivers characters and Eof in order, reading past timeouts") {
    val io =
      scripted(List('a', Timeout, 'b', Eof)).flatMap(read =>
        CharSource.pump(read).use { take =>
          for
            a <- take
            b <- take
            eof <- take
          yield (a, b, eof)
        }
      )
    assertEquals(run(io), ('a': Eof | Char, 'b': Eof | Char, Eof: Eof | Char))
  }

  test("The pump stops reading after Eof") {
    val io =
      for
        count <- Ref.of[IO, Int](0)
        read <- scripted(List('a', Eof))
        reads <- CharSource.pump(count.update(_ + 1) >> read).use { take =>
          take >> take >> IO.sleep(50.millis) >> count.get
        }
      yield reads
    assertEquals(run(io), 2)
  }
