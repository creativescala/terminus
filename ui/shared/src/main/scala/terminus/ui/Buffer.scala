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

import terminus.effect.AnsiCodes
import terminus.ui.style.Color
import terminus.ui.style.Style

/** A 2D grid of terminal cells that components render into.
  *
  * Coordinates are 0-based. Out-of-bounds writes are silently ignored,
  * providing isolation between components — a buggy component cannot corrupt
  * cells outside its allocated region.
  *
  * Call [[render]] once all components have written to the buffer to flush the
  * contents to the terminal.
  */
final class Buffer(val width: Int, val height: Int):
  // Row-major flat array: index = y * width + x
  private val cells: Array[Cell] = Array.fill(width * height)(Cell.empty)

  /** Write a single cell at (x, y). Out-of-bounds writes are ignored. */
  def put(x: Int, y: Int, cell: Cell): Unit =
    if x >= 0 && x < width && y >= 0 && y < height then
      cells(y * width + x) = cell

  /** Fill a rectangular region with a cell. Clips to buffer bounds. */
  def fill(rect: Rect, cell: Cell): Unit =
    val x0 = rect.x.max(0)
    val y0 = rect.y.max(0)
    val x1 = rect.right.min(width)
    val y1 = rect.bottom.min(height)
    var y = y0
    while y < y1 do
      var x = x0
      while x < x1 do
        cells(y * width + x) = cell
        x += 1
      y += 1

  /** Write a string horizontally starting at (x, y).
    *
    * Iterates over Unicode code points (not raw Java chars) so that characters
    * outside the Basic Multilingual Plane — including most emoji — are handled
    * correctly. Wide characters (display width 2) occupy two buffer cells: the
    * left cell holds the character and the right cell holds
    * [[Cell.continuation]] as a sentinel. Zero-width code points (combining
    * marks, variation selectors, ZWJ) are skipped; full grapheme cluster
    * composition is not currently supported. Clips to buffer bounds.
    */
  def putString(x: Int, y: Int, s: String, style: Style): Unit =
    var col = x
    var i = 0
    while i < s.length do
      val cp = Character.codePointAt(s, i)
      i += Character.charCount(cp)
      CharWidth.of(cp) match
        case 0 => () // zero-width: skip
        case 1 =>
          put(col, y, Cell(cp, style))
          col += 1
        case _ => // 2
          put(col, y, Cell(cp, style))
          put(col + 1, y, Cell.continuation)
          col += 2

  /** Flush the entire buffer to the terminal.
    *
    * Iterates cells row by row, emitting SGR attribute codes only when style
    * changes, and using absolute cursor positioning at the start of each row.
    * Continuation cells (right half of wide characters) are skipped. Emits a
    * full SGR reset before and after rendering.
    */
  def render(using t: Terminal): Unit =
    t.write(AnsiCodes.sgr("0"))
    var currentStyle = Style.default
    var y = 0
    while y < height do
      t.cursor.to(1, y + 1) // 1-based terminal coordinates
      var x = 0
      while x < width do
        val cell = cells(y * width + x)
        if cell.codePoint != 0 then // skip continuation cells
          currentStyle = emitStyle(currentStyle, cell.style)
          t.write(new String(Character.toChars(cell.codePoint)))
        x += 1
      y += 1
    t.write(AnsiCodes.sgr("0"))
    t.flush()

  /** Flush only cells that differ from [[previous]] to the terminal.
    *
    * Compares this buffer against [[previous]] cell by cell and emits only
    * changed positions, minimising the number of escape codes written. Cursor
    * movement is suppressed when changed cells are horizontally adjacent,
    * avoiding a `cursor.to` call for every single character in a long run of
    * changes.
    *
    * The two buffers must have the same dimensions. [[previous]] is not
    * modified; callers are responsible for swapping or copying buffers between
    * frames.
    */
  def renderDiff(previous: Buffer)(using t: Terminal): Unit =
    require(
      width == previous.width && height == previous.height,
      s"Buffer dimensions must match for diff render: ${width}x${height} vs ${previous.width}x${previous.height}"
    )
    t.write(AnsiCodes.sgr("0"))
    var currentStyle = Style.default
    // Track where the terminal cursor currently is (0-based).
    // (-1, -1) means "unknown / needs explicit move".
    var cursorX = -1
    var cursorY = -1
    var y = 0
    while y < height do
      var x = 0
      while x < width do
        val curr = cells(y * width + x)
        val prev = previous.cells(y * width + x)
        if curr != prev then
          if curr.codePoint != 0 then // skip continuation cells
            if !(cursorX == x && cursorY == y) then
              t.cursor.to(x + 1, y + 1) // 1-based
            currentStyle = emitStyle(currentStyle, curr.style)
            t.write(new String(Character.toChars(curr.codePoint)))
            // Wide chars advance the cursor by 2; narrow by 1
            val advance = CharWidth.of(curr.codePoint).max(1)
            cursorX = x + advance
            cursorY = y
        x += 1
      y += 1
    t.write(AnsiCodes.sgr("0"))
    t.flush()

  /** Emit SGR codes for any attributes that differ between [[from]] and [[to]],
    * and return [[to]] as the new current style.
    */
  private def emitStyle(from: Style, to: Style)(using t: Terminal): Style =
    if to != from then
      if to.fg != from.fg then t.write(fgCode(to.fg))
      if to.bg != from.bg then t.write(bgCode(to.bg))
      if to.bold != from.bold then
        t.write(if to.bold then AnsiCodes.format.bold.on
        else AnsiCodes.format.bold.off)
      if to.italic != from.italic then
        t.write(if to.italic then AnsiCodes.format.italic.on
        else AnsiCodes.format.italic.off)
      if to.underline != from.underline then
        t.write(underlineCode(to.underline))
      if to.blink != from.blink then
        t.write(if to.blink then AnsiCodes.format.blink.on
        else AnsiCodes.format.blink.off)
      if to.invert != from.invert then
        t.write(if to.invert then AnsiCodes.format.invert.on
        else AnsiCodes.format.invert.off)
      if to.strikethrough != from.strikethrough then
        t.write(if to.strikethrough then AnsiCodes.format.strikethrough.on
        else AnsiCodes.format.strikethrough.off)
    to

  private def underlineCode(underline: style.Underline): String =
    underline match
      case style.Underline.None     => AnsiCodes.format.underline.off
      case style.Underline.Straight => AnsiCodes.format.underline.straight
      case style.Underline.Double   => AnsiCodes.format.underline.double
      case style.Underline.Curly    => AnsiCodes.format.underline.curly
      case style.Underline.Dotted   => AnsiCodes.format.underline.dotted
      case style.Underline.Dashed   => AnsiCodes.format.underline.dashed

  private def fgCode(color: Color): String =
    color match
      case Color.Default       => AnsiCodes.foreground.default
      case Color.Black         => AnsiCodes.foreground.black
      case Color.Red           => AnsiCodes.foreground.red
      case Color.Green         => AnsiCodes.foreground.green
      case Color.Yellow        => AnsiCodes.foreground.yellow
      case Color.Blue          => AnsiCodes.foreground.blue
      case Color.Magenta       => AnsiCodes.foreground.magenta
      case Color.Cyan          => AnsiCodes.foreground.cyan
      case Color.White         => AnsiCodes.foreground.white
      case Color.BrightBlack   => AnsiCodes.foreground.brightBlack
      case Color.BrightRed     => AnsiCodes.foreground.brightRed
      case Color.BrightGreen   => AnsiCodes.foreground.brightGreen
      case Color.BrightYellow  => AnsiCodes.foreground.brightYellow
      case Color.BrightBlue    => AnsiCodes.foreground.brightBlue
      case Color.BrightMagenta => AnsiCodes.foreground.brightMagenta
      case Color.BrightCyan    => AnsiCodes.foreground.brightCyan
      case Color.BrightWhite   => AnsiCodes.foreground.brightWhite

  private def bgCode(color: Color): String =
    color match
      case Color.Default       => AnsiCodes.background.default
      case Color.Black         => AnsiCodes.background.black
      case Color.Red           => AnsiCodes.background.red
      case Color.Green         => AnsiCodes.background.green
      case Color.Yellow        => AnsiCodes.background.yellow
      case Color.Blue          => AnsiCodes.background.blue
      case Color.Magenta       => AnsiCodes.background.magenta
      case Color.Cyan          => AnsiCodes.background.cyan
      case Color.White         => AnsiCodes.background.white
      case Color.BrightBlack   => AnsiCodes.background.brightBlack
      case Color.BrightRed     => AnsiCodes.background.brightRed
      case Color.BrightGreen   => AnsiCodes.background.brightGreen
      case Color.BrightYellow  => AnsiCodes.background.brightYellow
      case Color.BrightBlue    => AnsiCodes.background.brightBlue
      case Color.BrightMagenta => AnsiCodes.background.brightMagenta
      case Color.BrightCyan    => AnsiCodes.background.brightCyan
      case Color.BrightWhite   => AnsiCodes.background.brightWhite
