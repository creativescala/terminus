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
import terminus.KeyCode
import terminus.KeyReader
import terminus.RawMode
import terminus.Writer
import terminus.effect
import terminus.ui.capability.Layout
import terminus.ui.component.Column
import terminus.ui.event.DefaultEvent
import terminus.ui.event.FocusId
import terminus.ui.layout.CellArrayBuffer
import terminus.ui.layout.Constraint
import terminus.ui.layout.DefaultLayout
import terminus.ui.layout.Measurement
import terminus.ui.layout.Size
import terminus.ui.react.DefaultReact
import terminus.ui.runtime.Runtime
import terminus.ui.style.LayoutProps

/** The root of a component tree. Acts as a column and renders into the
  * alternate screen of the terminal.
  */
class FullScreen(runtime: Runtime, column: Column):

  private[ui] def toBuffer()(using dims: effect.Dimensions): CellArrayBuffer =
    column.react(using DefaultReact.empty)
    val terminalSize = dims.getDimensions
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
                Key.shift(KeyCode.Tab) -> Seq(() => runtime.prevFocus()),
                Key.controlQ -> Seq(() => quit = true)
              )
            )

            def renderFrame(prev: CellArrayBuffer): CellArrayBuffer =
              val buf = toBuffer()
              if prev.width == buf.width && prev.height == buf.height then
                buf.renderDiff(prev)
              else
                InteractiveTerminal.erase.screen()
                buf.render

              buf

            def loop(prev: CellArrayBuffer): Unit =
              if quit then ()
              else
                val buf = renderFrame(prev)
                InteractiveTerminal.readKey() match
                  case terminus.Eof => quit = true
                  case key: Key     => runtime.dispatch(key)
                loop(buf)

            loop {
              InteractiveTerminal.erase.screen()
              val buf = toBuffer()
              buf.render
              buf
            }
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

  def apply(body: Layout ?=> Unit): FullScreen =
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
      body: Layout ?=> Unit
  ): FullScreen =
    val focusId = FocusId.next
    val runtime = Runtime.empty
    val context = new DefaultEvent(focusId, runtime)
      with DefaultLayout(runtime) {}
    // Evaluate body here so we do not retain a reference to it and it can be garbage collected.
    body(using context)
    val column = new Column(
      Size(Measurement.Percentage(1.0), Measurement.Percentage(1.0)),
      style(LayoutProps.default),
      context
    )
    val fullScreen = new FullScreen(runtime, column)

    fullScreen
