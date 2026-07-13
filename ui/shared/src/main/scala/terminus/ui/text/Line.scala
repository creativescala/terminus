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

import terminus.ui.layout.CharWidth

/** A `Line` is a single line of sanitized text. It is guaranteed to contain no
  * hard line breaks, no control characters, and no escape sequences, so it is
  * safe to render directly to a [[terminus.ui.layout.Buffer]].
  *
  * Construct a `Line` with [[Line.apply]], which sanitizes arbitrary input:
  * newlines become spaces, tabs are expanded, and escape sequences and other
  * control / bidirectional-override characters are removed.
  */
opaque type Line = String

object Line:

  /** Number of spaces a tab expands to (expansion is to the next tab stop). */
  private val TabWidth = 8

  private val Esc = '\u001b'
  private val Bel = '\u0007'

  /** Sanitize an arbitrary `String` into a single safe `Line`. Newlines are
    * replaced with a single space; use [[Text.apply]] if you want newlines to
    * be preserved as separate lines.
    */
  def apply(s: String): Line = sanitize(s)

  /** The empty Line. */
  val empty: Line = Line.unsafe("")

  /** Construct a `Line` without sanitizing it first. Should only be used as a
    * performance optimization for strings that are known to be safe.
    */
  private[text] def unsafe(s: String): Line = s

  extension (line: Line)

    /** Get the character at the given index. */
    def apply(idx: Int): Char =
      line.charAt(idx)

    /** The underlying `String`. */
    def value: String = line

    /** Convert this Line to Text. */
    def toText: Text = Text.fromLine(line)

    /** The display width of this line, in terminal cells (wide characters count
      * as two, combining / zero-width characters as zero).
      */
    def width: Int = displayWidth(line)

    /** The length of this line in terms of characters. See [[width]] for the
      * display width.
      */
    def length: Int = line.length

    /** Delete the character at the given position. If the position is outside
      * the Line the current Line is returned unchanged.
      */
    def delete(pos: Int): Line =
      if pos >= 0 && pos < line.length then
        line.substring(0, pos) ++ line.substring(pos + 1)
      else line

    /** Insert the character at the given position. `pos == length` appends to
      * the end of the Line. If the position is outside the Line the current
      * Line is returned unchanged.
      */
    def insert(pos: Int, c: Char): Line =
      if pos >= 0 && pos <= line.length then
        line.substring(0, pos) ++ c.toString ++ line.substring(pos)
      else line

    /** String.substring, but for a Line. Returns a new Line made up of a
      * subsequence of this sequence, beginning at the start index (inclusive)
      * and extending to the end index (exclusive).
      */
    def substring(start: Int, end: Int): Line =
      line.substring(start, end)

    /** Greedily word-wrap this line to fit within `width` cells.
      *
      * Words are separated by single spaces (runs of whitespace are collapsed
      * at break points). A word longer than `width` is hard-broken across
      * multiple lines. Always returns at least one (possibly empty) line.
      */
    def reflow(width: Int): Seq[Line] =
      if width < 1 then Vector(line)
      else
        val result = Vector.newBuilder[Line]
        val current = new StringBuilder
        var currentWidth = 0

        def flush(): Unit =
          result += current.toString
          current.setLength(0)
          currentWidth = 0

        for word <- line.split(' ') if word.nonEmpty do
          val w = displayWidth(word)
          if w <= width then
            val sep = if currentWidth == 0 then 0 else 1
            if currentWidth + sep + w > width then flush()
            if currentWidth > 0 then
              current.append(' ')
              currentWidth += 1
            current.append(word)
            currentWidth += w
          else
            // The word does not fit on a line of its own: hard-break it. All
            // but the last chunk become complete lines; the last chunk seeds
            // the current line so following words can continue on it.
            if current.nonEmpty then flush()
            val chunks = hardBreak(word, width)
            chunks.init.foreach(result += _)
            current.append(chunks.last)
            currentWidth = displayWidth(chunks.last)

        if current.nonEmpty then flush()
        val out = result.result()
        if out.isEmpty then Vector(("": Line)) else out

    def isEmpty: Boolean =
      line.isEmpty()

    def isNonEmpty: Boolean =
      !line.isEmpty()

  /** Break a single word with no break opportunities into chunks each at most
    * `width` cells wide. A wide character is never split across chunks.
    */
  private def hardBreak(word: String, width: Int): Vector[String] =
    val chunks = Vector.newBuilder[String]
    val sb = new StringBuilder
    var w = 0
    var i = 0
    while i < word.length do
      val cp = word.codePointAt(i)
      val cw = CharWidth.of(cp)
      val n = Character.charCount(cp)
      if w + cw > width && sb.nonEmpty then
        chunks += sb.toString
        sb.setLength(0)
        w = 0
      sb.append(word.substring(i, i + n))
      w += cw
      i += n
    if sb.nonEmpty then chunks += sb.toString
    chunks.result()

  private def displayWidth(s: String): Int =
    var w = 0
    var i = 0
    while i < s.length do
      val cp = s.codePointAt(i)
      w += CharWidth.of(cp)
      i += Character.charCount(cp)
    w

  /** Strip everything that would corrupt the terminal: whole escape sequences,
    * control characters, and bidirectional overrides. Tabs are expanded and
    * newlines flattened to spaces.
    */
  private def sanitize(s: String): String =
    val out = new StringBuilder(s.length)
    var col = 0
    var i = 0
    while i < s.length do
      val c = s.charAt(i)
      if c == Esc then i = skipEscape(s, i)
      else if c == '\t' then
        val spaces = TabWidth - (col % TabWidth)
        out.append(" " * spaces)
        col += spaces
        i += 1
      else if c == '\n' || c == '\r' then
        out.append(' ')
        col += 1
        i += 1
      else if isStripped(c) then i += 1
      else if c.isHighSurrogate && i + 1 < s.length && s
          .charAt(i + 1)
          .isLowSurrogate
      then
        val cp = Character.toCodePoint(c, s.charAt(i + 1))
        out.append(c).append(s.charAt(i + 1))
        col += CharWidth.of(cp)
        i += 2
      else
        out.append(c)
        col += CharWidth.of(c.toInt)
        i += 1
    out.toString

  /** Code points that must never reach the terminal. `\t`, `\n`, `\r` and `ESC`
    * are handled before this is consulted.
    */
  private def isStripped(c: Char): Boolean =
    val cp = c.toInt
    cp < 0x20 || // C0 control codes
    cp == 0x7f || // DEL
    (cp >= 0x80 && cp <= 0x9f) || // C1 control codes
    (cp >= 0x202a && cp <= 0x202e) || // bidi embeddings / overrides
    (cp >= 0x2066 && cp <= 0x2069) // bidi isolates (Trojan Source)

  private def isCsiFinal(c: Char): Boolean = c >= '@' && c <= '~'

  /** Given that `s.charAt(start) == ESC`, return the index immediately after
    * the escape sequence so the whole thing is dropped, not just the ESC byte.
    */
  private def skipEscape(s: String, start: Int): Int =
    var i = start + 1
    if i >= s.length then i // lone trailing ESC
    else
      s.charAt(i) match
        case '[' => // CSI: ESC [ params ... final byte in 0x40-0x7e
          i += 1
          while i < s.length && !isCsiFinal(s.charAt(i)) do i += 1
          if i < s.length then i + 1 else i
        case ']' => // OSC: ESC ] ... terminated by BEL or ST (ESC \)
          i += 1
          var done = false
          while i < s.length && !done do
            val c = s.charAt(i)
            if c == Bel then
              i += 1
              done = true
            else if c == Esc && i + 1 < s.length && s.charAt(i + 1) == '\\' then
              i += 2
              done = true
            else i += 1
          i
        case _ => i + 1 // two-character escape: ESC + one byte
