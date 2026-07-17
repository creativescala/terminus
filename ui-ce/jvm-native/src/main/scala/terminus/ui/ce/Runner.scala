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
import cats.effect.std.Supervisor
import terminus.ce.CharSource
import terminus.effect
import terminus.ui.FullScreen as UiFullScreen
import terminus.ui.FullScreen.InteractiveTerminal
import terminus.ui.react.WritableSignal
import terminus.ui.runtime.Event

/** A Cats Effect runner for full screen applications: the concurrent
  * counterpart of the blocking `FullScreen.run`.
  *
  * Where the blocking runner's loop blocks on `readKey`, this runner blocks on
  * an event queue: a producer fiber reads keys and offers them, timer fibers
  * offer `Event.Effect`s, and future producers — resize notifications,
  * application events — do the same to wake the loop without a key press.
  */
object Runner:
  /** The blocking runner's terminal requirements, plus the timed read that
    * feeds the char pump.
    */
  type CeTerminal = UiFullScreen.InteractiveTerminal & effect.NonBlockingReader

  /** Run a [[FullScreen]] built with the [[terminus.ui.capability.Timer]]
    * capability in scope.
    */
  def run(fullScreen: FullScreen, terminal: CeTerminal): IO[Unit] =
    run(fullScreen.underlying, fullScreen.timer, terminal)

  /** Run a plain `terminus.ui.FullScreen`. The application cannot use timers —
    * it never had the [[terminus.ui.capability.Timer]] capability in scope —
    * but the same application then also runs on the blocking runner.
    */
  def run(fullScreen: UiFullScreen, terminal: CeTerminal): IO[Unit] =
    run(fullScreen, new DefaultTimer, terminal)

  private def run(
      fullScreen: UiFullScreen,
      timer: DefaultTimer,
      terminal: CeTerminal
  ): IO[Unit] =
    // Core's terminal modes are synchronous brackets (raw(f), etc.), so the
    // session is bracketed on a blocking thread which then runs the
    // concurrent part of the session with unsafeRunSync. A consequence is
    // that canceling this IO does not stop the session; quitting comes from
    // within (Ctrl+Q or Eof). Terminal modes usable directly from effect
    // systems are #27, at which point this inversion can go.
    //
    // The dispatcher must be parallel: timer spawns are dispatched from
    // within the session, which a sequential dispatcher would queue behind
    // the session itself, never to run.
    Dispatcher.parallel[IO].use { dispatcher =>
      IO.blocking {
        given UiFullScreen.InteractiveTerminal = terminal
        InteractiveTerminal.cursor.hidden {
          InteractiveTerminal.raw {
            InteractiveTerminal.alternateScreen {
              dispatcher.unsafeRunSync(
                session(fullScreen, timer, terminal, dispatcher)
              )
            }
          }
        }
      }
    }

  private def session(
      fullScreen: UiFullScreen,
      timer: DefaultTimer,
      terminal: CeTerminal,
      dispatcher: Dispatcher[IO]
  )(using
      UiFullScreen.RenderTerminal
  ): IO[Unit] =
    for
      queue <- Queue.unbounded[IO, Event]
      // The terminal size is a reactive input, exactly as in the blocking
      // runner. Until resize notifications become a producer of their own the
      // consumer refreshes it before every step.
      terminalSize <- IO(WritableSignal(terminal.getDimensions))
      step <- IO(fullScreen.eventLoop(terminalSize))
      // The supervisor owns the timer fibers, canceling any still running
      // when the session ends. Timer.after and Timer.every are synchronous
      // calls made from setup code and event handlers, so spawning goes
      // through the dispatcher.
      _ <- Supervisor[IO].use { supervisor =>
        val spawn: DefaultTimer.Task => Unit = task =>
          dispatcher.unsafeRunAndForget(
            supervisor.supervise(serve(task, queue)).void
          )

        IO(timer.connect(spawn)) >>
          CharSource.fromReader(terminal).use { chars =>
            Events.keys(chars, queue).background.surround {
              Events.consume(
                queue,
                beforeStep = IO(terminalSize.set(terminal.getDimensions)),
                step
              )
            }
          }
      }
    yield ()

  /** The fiber serving one timer task: ticks and one-shots reach the reactive
    * graph as `Event.Effect`s on the queue, so they are writes on the loop like
    * any other.
    */
  private def serve(
      task: DefaultTimer.Task,
      queue: Queue[IO, Event]
  ): IO[Unit] =
    task match
      case DefaultTimer.Task.Every(interval, ticks) =>
        (IO.sleep(interval) >>
          queue.offer(Event.Effect(() => ticks.update(_ + 1)))).foreverM
      case DefaultTimer.Task.After(delay, run) =>
        IO.sleep(delay) >> queue.offer(Event.Effect(run))

extension (fullScreen: FullScreen)
  /** Run this full screen application on [[Runner]]. */
  def run(terminal: Runner.CeTerminal): IO[Unit] =
    Runner.run(fullScreen, terminal)
