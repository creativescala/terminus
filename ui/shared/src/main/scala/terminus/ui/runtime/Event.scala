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

package terminus.ui.runtime

import terminus.Eof
import terminus.Key

/** A single event consumed by the event loop. Everything that drives the loop —
  * key presses, and writes originating outside it — arrives as one of these, so
  * the reactive graph is only ever touched by the loop's single consumer.
  */
private[ui] enum Event:
  /** A key press, or end of input, to dispatch to key handlers. */
  case Input(key: Eof | Key)

  /** An effect to run on the loop. This is how signal writes from outside the
    * loop — timer ticks, resize notifications, application events — are
    * marshaled onto it.
    */
  case Effect(run: () => Unit)
