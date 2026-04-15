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

import terminus.NativeTerminal
import terminus.ui.component.Text

@main def boxes(): Unit =
  val program: FullScreen.Program[Unit] =
    FullScreen {
      Row {
        Text(20, 5)("Box A")
        Text(20, 5)("Box B")
      }
      Text(20, 5)("Box C")
    }

  NativeTerminal.run {
    program
    Terminal.newline
  }
