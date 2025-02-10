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

opaque type KeyModifier = Byte
object KeyModifier {
  val Shift: KeyModifier = 0x0001
  val Control: KeyModifier = 0x0002
  val Alt: KeyModifier = 0x0004
  val Super: KeyModifier = 0x0008
  val Hyper: KeyModifier = 0x0010
  val Meta: KeyModifier = 0x0020
  val None: KeyModifier = 0x0000
}
extension (modifier: KeyModifier) {
  def and(other: KeyModifier): KeyModifier =
    (modifier & other).toByte

  def or(other: KeyModifier): KeyModifier =
    (modifier | other).toByte

  def hasControl: Boolean =
    modifier.and(KeyModifier.Control) != KeyModifier.None

  def hasShift: Boolean =
    modifier.and(KeyModifier.Shift) != KeyModifier.None

  def hasAlt: Boolean =
    modifier.and(KeyModifier.Alt) != KeyModifier.None

  def hasSuper: Boolean =
    modifier.and(KeyModifier.Super) != KeyModifier.None

  def hasHyper: Boolean =
    modifier.and(KeyModifier.Hyper) != KeyModifier.None

  def hasMeta: Boolean =
    modifier.and(KeyModifier.Meta) != KeyModifier.None
}
