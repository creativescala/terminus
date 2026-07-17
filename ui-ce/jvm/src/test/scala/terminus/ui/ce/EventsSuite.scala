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

package terminus.ui.ce

import cats.effect.IO
import cats.effect.Ref
import cats.effect.std.Queue
import cats.effect.unsafe.implicits.global
import munit.FunSuite
import terminus.Eof
import terminus.Key
import terminus.effect.TerminalDimensions
import terminus.ui.runtime.Event

import scala.collection.mutable
import scala.concurrent.duration.*

class EventsSuite extends FunSuite:
  def run[A](io: IO[A]): A = io.timeout(5.seconds).unsafeRunSync()

  /** A char source backed by a queue preloaded with `input`; reads beyond the
    * end block.
    */
  def charsOf(input: (Eof | Char)*): IO[IO[Eof | Char]] =
    for
      queue <- Queue.unbounded[IO, Eof | Char]
      _ <- input.toList.foldLeft(IO.unit)((io, c) => io >> queue.offer(c))
    yield queue.take

  test("keys offers parsed keys and finishes after offering Eof") {
    val io =
      for
        chars <- charsOf('a', '\u001b', '[', 'A', Eof)
        queue <- Queue.unbounded[IO, Event]
        _ <- Events.keys(chars, queue)
        first <- queue.take
        second <- queue.take
        third <- queue.take
      yield (first, second, third)

    assertEquals(
      run(io),
      (
        Event.Input(Key('a')): Event,
        Event.Input(Key.up): Event,
        Event.Input(Eof): Event
      )
    )
  }

  test("consume steps each event in order until step returns false") {
    val stop = Event.Input(Eof)
    val events =
      List(Event.Input(Key('a')), Event.Effect(() => ()), stop)
    // The step function is synchronous by design, so plain mutable state is
    // the natural way to observe it: only the single consumer touches it.
    val seen = mutable.ListBuffer.empty[Event]

    val io =
      for
        queue <- Queue.unbounded[IO, Event]
        _ <- events.foldLeft(IO.unit)((io, e) => io >> queue.offer(e))
        _ <- Events.consume(
          queue,
          step = event =>
            seen += event
            event != stop
        )
      yield seen.toList

    assertEquals(run(io), events)
  }

  test("resizes offers an event only when the dimensions change") {
    // Polls of unchanged dimensions produce nothing; the change produces one
    // event carrying the new value.
    val script = List(
      TerminalDimensions(80, 24),
      TerminalDimensions(80, 24),
      TerminalDimensions(80, 24),
      TerminalDimensions(120, 40)
    )
    var applied: Option[TerminalDimensions] = None

    val io =
      for
        remaining <- Ref.of[IO, List[TerminalDimensions]](script)
        dimensions = remaining.modify {
          case head :: tail => (tail, head)
          case Nil          => (Nil, script.last)
        }
        queue <- Queue.unbounded[IO, Event]
        producer <- Events
          .resizes(dimensions, d => applied = Some(d), queue, 1.milli)
          .start
        event <- queue.take
        _ <- producer.cancel
      yield event

    run(io) match
      case Event.Effect(update) =>
        update()
        assertEquals(applied, Some(TerminalDimensions(120, 40)))
      case other => fail(s"Unexpected event: $other")
  }
