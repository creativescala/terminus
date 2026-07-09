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

package terminus.ui.text

/** A `Text` is a block of sanitized text: a sequence of logical [[Line]]s split
  * on the hard line breaks present in the original input. Blank lines (from
  * consecutive newlines) are preserved.
  *
  * `Text` holds the *logical* lines — the author's hard breaks. Call
  * [[Text.reflow]] to produce the *physical* lines that fit a given width.
  * Always reflow from the logical `Text`, never from a previous reflow result,
  * so that a width change (e.g. a terminal resize) re-wraps correctly rather
  * than baking in stale soft breaks.
  */
opaque type Text = Vector[Line]

object Text:

  /** Parse arbitrary input into `Text`. Line endings are normalized (`\r\n` and
    * lone `\r` both count as a break) and each resulting line is sanitized via
    * [[Line.apply]].
    */
  def apply(s: String): Text =
    val normalized = s.replace("\r\n", "\n").replace('\r', '\n')
    // limit -1 keeps trailing empty strings, so blank lines are preserved.
    // unsafe avoids sanitizing strings that are already sanitized
    normalized.split("\n", -1).iterator.map(Line.apply).toVector

  def fromLine(l: Line): Text =
    Vector(l)

  extension (text: Text)

    /** The logical lines, with hard breaks preserved and no soft wrapping. */
    def lines: Seq[Line] = text

    /** Reflow every logical line to `width` cells and concatenate the results.
      */
    def reflow(width: Int): Seq[Line] =
      text.flatMap(_.reflow(width))
