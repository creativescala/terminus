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

import terminus.ui
import terminus.ui.capability.Layout
import terminus.ui.capability.React
import terminus.ui.capability.Timer
import terminus.ui.component.Column
import terminus.ui.event.DefaultEvent
import terminus.ui.event.FocusId
import terminus.ui.layout.DefaultLayout
import terminus.ui.layout.Measurement
import terminus.ui.layout.Size
import terminus.ui.react.DefaultReact
import terminus.ui.react.Signal
import terminus.ui.runtime.Runtime
import terminus.ui.style.LayoutProps

import scala.concurrent.duration.FiniteDuration

/** The root of a component tree for the Cats Effect runner: as
  * [[terminus.ui.FullScreen]], but the application body additionally receives
  * the [[Timer]] capability, which only a runtime with a notion of time can
  * provide. Run it with `Runner.run` (or the `run` extension method).
  */
final class FullScreen private (
    private[ce] val underlying: ui.FullScreen,
    private[ce] val timer: DefaultTimer
)

object FullScreen:
  def apply(body: (Layout & React & Timer) ?=> Unit): FullScreen =
    withLayout(identity)(ctx ?=> body(using ctx))

  /** As [[apply]], but also configures the layout of the root column; see
    * `terminus.ui.FullScreen.withLayout`.
    */
  def withLayout(style: LayoutProps => LayoutProps)(
      body: (Layout & React & Timer) ?=> Unit
  ): FullScreen =
    val focusId = FocusId.next
    val runtime = Runtime.empty
    val timer = new DefaultTimer
    val context = new DefaultEvent(focusId, runtime)
      with DefaultLayout(runtime)
      with DefaultReact(runtime)
      with Timer:
      def every(interval: FiniteDuration): Signal[Long] =
        timer.every(interval)
      def after(delay: FiniteDuration)(f: () => Unit): Unit =
        timer.after(delay)(f)
    // Evaluate body here so we do not retain a reference to it and it can be
    // garbage collected.
    body(using context)
    val column = new Column(
      Size(Measurement.Percentage(1.0), Measurement.Percentage(1.0)),
      style(LayoutProps.default),
      context
    )

    new FullScreen(new ui.FullScreen(runtime, column), timer)
