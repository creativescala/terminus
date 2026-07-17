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
import cats.effect.Resource
import terminus.JLineTerminal

// Run with: sbt 'uiCeJVM/runMain terminus.ui.ce.demo'
//
// See DemoApp for what the demo shows.
object demo extends IOApp.Simple:
  def run: IO[Unit] =
    Resource
      .make(IO(JLineTerminal.apply))(terminal => IO(terminal.close()))
      .use(terminal => DemoApp.make.run(terminal))
