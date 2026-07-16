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

import terminus.AlternateScreenMode
import terminus.Cursor
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
import terminus.ui.component.Column
import terminus.ui.event.DefaultEvent
import terminus.ui.event.FocusId
import terminus.ui.layout.CellArrayBuffer
import terminus.ui.layout.Constraint
import terminus.ui.layout.DefaultLayout
import terminus.ui.layout.Measurement
import terminus.ui.layout.Size
import terminus.ui.react.DefaultReact
import terminus.ui.react.Effect
import terminus.ui.react.WritableSignal
import terminus.ui.runtime.Runtime
import terminus.ui.style.LayoutProps

/** The root of a component tree. Acts as a column and renders into the
  * alternate screen of the terminal.
  */
class FullScreen(runtime: Runtime, column: Column):

  private[ui] def toBuffer(terminalSize: TerminalDimensions)(using
      Observe
  ): CellArrayBuffer =
    val constraint = Constraint.tight(terminalSize.columns, terminalSize.rows)
    val contentDimensions = column.measure(constraint)
    val buf = CellArrayBuffer(contentDimensions.width, contentDimensions.height)
    column.render(contentDimensions, buf)
    buf

  def run(terminal: FullScreen.InteractiveTerminal): Unit =
    import FullScreen.InteractiveTerminal

    val program: InteractiveTerminal ?=> Unit =
      InteractiveTerminal.cursor.hidden {
        InteractiveTerminal.raw {
          InteractiveTerminal.alternateScreen {

            var quit = false

            // Setup default handlers
            runtime.addRootHandlers(
              Map(
                Key.tab -> Seq(() => runtime.nextFocus()),
                Key.shiftTab -> Seq(() => runtime.prevFocus()),
                Key.controlQ -> Seq(() => quit = true)
              )
            )

            // The terminal size is a reactive input like any other: the
            // render effect reads it, so a resize schedules a redraw the same
            // way a signal written by a key handler does. The loop refreshes
            // it after every key.
            val terminalSize =
              WritableSignal(terminal.getDimensions)

            var prev: Option[CellArrayBuffer] = None

            // Rendering is an effect: the frame is drawn when a reactive read
            // anywhere in measure or render has changed, and not otherwise.
            // Constructing the effect draws the first frame and subscribes to
            // everything that frame read.
            Effect(runtime.effectQueue) {
              val buf = toBuffer(terminalSize.get)
              prev match
                case Some(p)
                    if p.width == buf.width && p.height == buf.height =>
                  buf.renderDiff(p)
                case _ =>
                  InteractiveTerminal.erase.screen()
                  buf.render
              prev = Some(buf)
            }

            def loop(): Unit =
              if quit then ()
              else
                InteractiveTerminal.readKey() match
                  case terminus.Eof => quit = true
                  case key: Key     => runtime.dispatch(key)
                if !quit then
                  terminalSize.set(terminal.getDimensions)
                  runtime.effectQueue.drain()
                loop()

            loop()
          }
        }
      }

    program(using terminal)

object FullScreen:
  type InteractiveTerminal = effect.AlternateScreenMode & effect.Erase &
    effect.Cursor & effect.Writer & effect.KeyReader & effect.RawMode &
    effect.Dimensions
  object InteractiveTerminal
      extends AlternateScreenMode,
        Cursor,
        Erase,
        KeyReader,
        RawMode,
        Writer

  def apply(body: (Layout & React) ?=> Unit): FullScreen =
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
      body: (Layout & React) ?=> Unit
  ): FullScreen =
    val focusId = FocusId.next
    val runtime = Runtime.empty
    val context = new DefaultEvent(focusId, runtime)
      with DefaultLayout(runtime)
      with DefaultReact(runtime) {}
    // Evaluate body here so we do not retain a reference to it and it can be garbage collected.
    body(using context)
    val column = new Column(
      Size(Measurement.Percentage(1.0), Measurement.Percentage(1.0)),
      style(LayoutProps.default),
      context
    )
    val fullScreen = new FullScreen(runtime, column)

    fullScreen
