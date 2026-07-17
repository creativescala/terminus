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
import cats.effect.IOApp
import terminus.Key
import terminus.NativeTerminal
import terminus.ui.FullScreen
import terminus.ui.component.Column
import terminus.ui.component.Text
import terminus.ui.layout.Size
import terminus.ui.react.Signal
import terminus.ui.text

// Run with: sbt 'uiCeNative/runMain terminus.ui.ce.demo'
//
// The Native twin of the JVM demo: the same counter through the Cats Effect
// runner, driving NativeTerminal.
object demo extends IOApp.Simple:
  def run: IO[Unit] =
    val fullScreen = FullScreen { ctx ?=>
      val count = ctx.signal(0)

      Column(Size.fixed(40, 2)) { ctx ?=>
        ctx.onKey(Key.up)(count.update(_ + 1))
        ctx.onKey(Key.down)(count.update(_ - 1))

        Text(Size.fixed(40, 1), _.withBox(_.withoutBorder)) { ctx ?=>
          ctx.computed(text.Text(s"Count: ${count.get}"))
        }
        Text(Size.fixed(40, 1), _.withBox(_.withoutBorder)) {
          Signal.constant(text.Text("↑/↓ change · Ctrl+Q quit"))
        }
      }
    }

    Runner.run(fullScreen, NativeTerminal)
