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

package terminus.ui

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import terminus.AlternateScreenMode
import terminus.Cursor
import terminus.Eof
import terminus.Erase
import terminus.Key
import terminus.KeyReader
import terminus.RawMode
import terminus.Writer
import terminus.effect
import terminus.effect.TerminalDimensions
import terminus.ui.capability.Layout
import terminus.ui.capability.Observe
import terminus.ui.capability.React
import terminus.ui.capability.Timer
import terminus.ui.component.Column
import terminus.ui.event.DefaultEvent
import terminus.ui.event.FocusId
import terminus.ui.layout.CellArrayBuffer
import terminus.ui.layout.Constraint
import terminus.ui.layout.DefaultLayout
import terminus.ui.layout.Measurement
import terminus.ui.layout.Size
import terminus.ui.react.DefaultReact
import terminus.ui.runtime.CatsEffectRunner
import terminus.ui.runtime.DefaultRuntime
import terminus.ui.runtime.Event
import terminus.ui.runtime.Runner
import terminus.ui.runtime.Runtime
import terminus.ui.style.LayoutProps
import terminus.ui.timer.DefaultTimer

import scala.annotation.tailrec

/** The root of a component tree. Acts as a column and renders into the
  * alternate screen of the terminal.
  */
class FullScreen(runner: Runner, runtime: Runtime, column: Column):

  private[ui] def toBuffer(terminalSize: TerminalDimensions)(using
      Observe
  ): CellArrayBuffer =
    val constraint = Constraint.tight(terminalSize.columns, terminalSize.rows)
    val contentDimensions = column.measure(constraint)
    val buf = CellArrayBuffer(contentDimensions.width, contentDimensions.height)
    column.render(contentDimensions, buf)
    buf

  def run(using
      terminal: FullScreen.InteractiveTerminal
  ): Unit =
    import FullScreen.InteractiveTerminal

    InteractiveTerminal.cursor.hidden {
      InteractiveTerminal.raw {
        InteractiveTerminal.alternateScreen {

          // Setup default handlers
          runtime.addRootHandlers(
            Map(
              Key.tab -> Seq(() => runtime.nextFocus()),
              Key.shiftTab -> Seq(() => runtime.prevFocus()),
              Key.controlQ -> Seq(() => runtime.quit())
            )
          )

          given Observe = Observe.empty
          var currentSize: TerminalDimensions = terminal.getDimensions
          var prevBuffer: CellArrayBuffer = toBuffer(currentSize)

          val queue = runner.run(terminal)

          def render(): Unit =
            val buf = toBuffer(currentSize)

            if prevBuffer.width == buf.width && prevBuffer.height == buf.height
            then buf.renderDiff(prevBuffer)
            else
              terminal.erase.screen()
              buf.render

            prevBuffer = buf

          def step(): Unit =
            if runtime.quitRequested then ()
            else
              val event = queue.take()
              event match
                case Event.Input(Eof)      => runtime.quit()
                case Event.Input(key: Key) => runtime.dispatch(key)
                case Event.Effect(run)     => run.run()
                case Event.Resize(dim)     => currentSize = dim

          @tailrec
          def loop(): Unit =
            if runtime.quitRequested then ()
            else
              step()
              render()
              loop()

          render()
          loop()
        }
      }
    }
object FullScreen:
  type InteractiveTerminal = effect.AlternateScreenMode & effect.Cursor &
    effect.Erase & effect.Dimensions & effect.KeyReader &
    effect.NonBlockingReader & effect.RawMode & effect.Writer

  object InteractiveTerminal
      extends AlternateScreenMode,
        Cursor,
        Erase,
        KeyReader,
        RawMode,
        Writer

  extension (app: IO[FullScreen])
    def unsafeRun(using terminal: InteractiveTerminal): Unit =
      import cats.effect.unsafe.implicits.global
      app.flatMap(f => IO.blocking(f.run(using terminal))).unsafeRunAndForget()

  def apply(body: (Layout & React & Timer) ?=> Unit): IO[FullScreen] =
    withLayout(identity)(ctx ?=> body(using ctx))

  /** As [[apply]], but also configures the layout of the root column that holds
    * the component tree. The root column always fills the terminal; `style`
    * controls how children are placed within it (e.g.
    * `_.withAlign(Align.Start)` lets children take their natural width instead
    * of being stretched to the terminal's).
    *
    * This is a separate method rather than an overload of [[apply]] because
    * overloading would stop `FullScreen { ... }` blocks being adapted to the
    * `Layout ?=> Unit` context function type.
    */
  def withLayout(style: LayoutProps => LayoutProps)(
      body: (Layout & React & Timer) ?=> Unit
  ): IO[FullScreen] =
    CatsEffectRunner.apply.map { runner =>
      val focusId = FocusId.next
      val runtime = DefaultRuntime(runner)
      val context = new DefaultEvent(focusId, runtime)
        with DefaultLayout(runtime)
        with DefaultReact(runtime)
        with DefaultTimer(runtime) {}
      // Evaluate body here so we do not retain a reference to it and it can be garbage collected.
      body(using context)
      val column = new Column(
        Size(Measurement.Percentage(1.0), Measurement.Percentage(1.0)),
        style(LayoutProps.default),
        context
      )
      val fullScreen = new FullScreen(runner, runtime, column)

      fullScreen
    }

  /** A convenience to create a FullScreen application and immediately run it.
    */
  def applyAndRun(body: (Layout & React) ?=> Unit)(using
      InteractiveTerminal,
      IORuntime
  ): Unit =
    apply(body)
      .flatMap(fullScreen => IO.blocking(fullScreen.run))
      .unsafeRunAndForget()
