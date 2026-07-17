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
import cats.effect.std.Dispatcher
import cats.effect.std.Queue
import terminus.ce.CharSource
import terminus.effect
import terminus.ui.FullScreen
import terminus.ui.FullScreen.InteractiveTerminal
import terminus.ui.react.WritableSignal
import terminus.ui.runtime.Event

/** A Cats Effect runner for [[terminus.ui.FullScreen]]: the concurrent
  * counterpart of the blocking `FullScreen.run`.
  *
  * Where the blocking runner's loop blocks on `readKey`, this runner blocks on
  * an event queue: a producer fiber reads keys and offers them, and future
  * producers — timers, resize notifications, application events — can offer
  * `Event.Effect`s to wake the loop without a key press.
  */
object Runner:
  /** The blocking runner's terminal requirements, plus the timed read that
    * feeds the char pump.
    */
  type CeTerminal = FullScreen.InteractiveTerminal & effect.NonBlockingReader

  def run(fullScreen: FullScreen, terminal: CeTerminal): IO[Unit] =
    // Core's terminal modes are synchronous brackets (raw(f), etc.), so the
    // session is bracketed on a blocking thread which then runs the
    // concurrent part of the session with unsafeRunSync. A consequence is
    // that canceling this IO does not stop the session; quitting comes from
    // within (Ctrl+Q or Eof). Terminal modes usable directly from effect
    // systems are #27, at which point this inversion can go.
    Dispatcher.sequential[IO].use { dispatcher =>
      IO.blocking {
        given FullScreen.InteractiveTerminal = terminal
        InteractiveTerminal.cursor.hidden {
          InteractiveTerminal.raw {
            InteractiveTerminal.alternateScreen {
              dispatcher.unsafeRunSync(session(fullScreen, terminal))
            }
          }
        }
      }
    }

  private def session(fullScreen: FullScreen, terminal: CeTerminal)(using
      FullScreen.RenderTerminal
  ): IO[Unit] =
    for
      queue <- Queue.unbounded[IO, Event]
      // The terminal size is a reactive input, exactly as in the blocking
      // runner. Until resize notifications become a producer of their own the
      // consumer refreshes it before every step.
      terminalSize <- IO(WritableSignal(terminal.getDimensions))
      step <- IO(fullScreen.eventLoop(terminalSize))
      _ <- CharSource.fromReader(terminal).use { chars =>
        Events.keys(chars, queue).background.surround {
          Events.consume(
            queue,
            beforeStep = IO(terminalSize.set(terminal.getDimensions)),
            step
          )
        }
      }
    yield ()
