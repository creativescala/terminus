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

import terminus.Key
import terminus.ui.component.Column
import terminus.ui.component.Text
import terminus.ui.layout.Size
import terminus.ui.react.Signal
import terminus.ui.text

import scala.concurrent.duration.*

/** The demo application, shared by the per-platform demo mains.
  *
  * The point of the demo is the spinner: a terminal UI animating with no key
  * presses, which the blocking runner cannot do. The counter shows keys and
  * timers coexisting, and the status line shows a one-shot timer.
  */
private[ce] object DemoApp:
  val frames = Vector("⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏")

  def make: FullScreen =
    FullScreen { ctx ?=>
      val count = ctx.signal(0)
      val ticks = ctx.every(100.millis)
      val status = ctx.signal("The one-shot timer has not fired yet…")
      ctx.after(5.seconds)(() => status.set("The one-shot timer fired ✓"))

      Column(Size.fixed(44, 4)) { ctx ?=>
        ctx.onKey(Key.up)(count.update(_ + 1))
        ctx.onKey(Key.down)(count.update(_ - 1))

        Text(Size.fixed(44, 1), _.withBox(_.withoutBorder)) { ctx ?=>
          ctx.computed {
            val frame = frames((ticks.get % frames.size).toInt)
            text.Text(s"$frame Spinning, no key presses needed")
          }
        }
        Text(Size.fixed(44, 1), _.withBox(_.withoutBorder)) { ctx ?=>
          ctx.computed(text.Text(status.get))
        }
        Text(Size.fixed(44, 1), _.withBox(_.withoutBorder)) { ctx ?=>
          ctx.computed(text.Text(s"Count: ${count.get}"))
        }
        Text(Size.fixed(44, 1), _.withBox(_.withoutBorder)) {
          Signal.constant(text.Text("↑/↓ change · Ctrl+Q quit"))
        }
      }
    }
