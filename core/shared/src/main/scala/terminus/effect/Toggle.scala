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

package terminus.effect

/** A `Toggle` stores state for Terminal effects that involve turning on some
  * state within a block of code and turning it off when that block exits. A
  * typical example is text styling: we have a block where we want invert
  * styling enabled, and when the block finishes we want inverted styling turned
  * off. A complication arises when we can nest blocks. An inverted block might
  * be nested inside another inverted block. It's only when the outer block
  * exits that the styling should be turned off. A `Toggle` handles this with a
  * counter. Only when the counter hits zero do we emit the reset escape code.
  *
  * @param `set`:
  *   The escape code to emit when entering a block.
  * @param `reset`:
  *   The escape code to emit when exiting a block.
  */
final class Toggle(set: String, reset: String) {
  private var count: Int = 0

  /** Toggle on the effect, indicating we're entering a block. */
  def on(writer: Writer): Unit = {
    if count == 0 then writer.write(set)
    count = count + 1
  }

  /** Toggle off the effect, indicating we're exiting a block. */
  def off(writer: Writer): Unit = {
    if count == 1 then writer.write(reset)
    count = count - 1
  }
}
