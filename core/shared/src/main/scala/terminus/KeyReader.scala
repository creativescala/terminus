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

package terminus

trait KeyReader {

  /** Read a [[Key]] from the terminal.
    *
    * This method converts the most common escape code sent by the terminal into
    * the more useful representation defined in [[Key]]. It is a blocking
    * operation, and will not return until a key has been read or end-of-file
    * (EOF) is received.
    */
  def readKey(): effect.KeyReader ?=> Eof | Key =
    effect ?=> effect.readKey()
}
