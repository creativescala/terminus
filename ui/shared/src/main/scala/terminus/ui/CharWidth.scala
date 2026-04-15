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

/** Terminal display width of Unicode code points.
  *
  * Terminals render characters into cells. Most characters occupy one cell, but
  * East Asian Wide characters (CJK ideographs, many emoji, etc.) occupy two,
  * and combining/zero-width characters occupy zero.
  *
  * The ranges here follow Unicode TR#11 (East Asian Width) and the widely-used
  * wcwidth conventions. They cover the most common wide characters: CJK
  * ideographs, Hangul, Japanese kana, fullwidth forms, and emoji.
  */
object CharWidth:

  /** Returns the number of terminal columns a code point occupies: 0, 1, or 2.
    *
    * Zero-width code points (combining marks, variation selectors, ZWJ) should
    * be attached to the preceding character; callers that do not support
    * grapheme cluster composition may ignore them.
    */
  def of(codePoint: Int): Int =
    if isZeroWidth(codePoint) then 0
    else if isWide(codePoint) then 2
    else 1

  private def isZeroWidth(cp: Int): Boolean =
    cp == 0 ||
      // Combining Diacritical Marks
      (cp >= 0x0300 && cp <= 0x036f) ||
      // Combining Diacritical Marks Extended / Supplement
      (cp >= 0x1ab0 && cp <= 0x1aff) ||
      (cp >= 0x1dc0 && cp <= 0x1dff) ||
      // Hebrew / Arabic combining
      (cp >= 0x0591 && cp <= 0x05bd) ||
      cp == 0x05bf ||
      (cp >= 0x05c1 && cp <= 0x05c2) ||
      (cp >= 0x05c4 && cp <= 0x05c5) ||
      cp == 0x05c7 ||
      (cp >= 0x0610 && cp <= 0x061a) ||
      (cp >= 0x064b && cp <= 0x065f) ||
      cp == 0x0670 ||
      (cp >= 0x06d6 && cp <= 0x06e4) ||
      (cp >= 0x06e7 && cp <= 0x06e8) ||
      (cp >= 0x06ea && cp <= 0x06ed) ||
      // Zero-width spaces / joiners
      (cp >= 0x200b && cp <= 0x200f) ||
      cp == 0x200d || // Zero Width Joiner
      // Variation Selectors (text/emoji presentation)
      (cp >= 0xfe00 && cp <= 0xfe0f) ||
      (cp >= 0xe0100 && cp <= 0xe01ef) ||
      // BOM / Zero Width No-Break Space
      cp == 0xfeff ||
      // Combining Half Marks
      (cp >= 0xfe20 && cp <= 0xfe2f) ||
      // Tags block (used in emoji flag sequences)
      (cp >= 0xe0000 && cp <= 0xe007f)

  private def isWide(cp: Int): Boolean =
    // Hangul Jamo
    (cp >= 0x1100 && cp <= 0x115f) ||
      // CJK brackets
      cp == 0x2329 || cp == 0x232a ||
      // Miscellaneous Symbols (☀ ⚠ etc.) — many displayed as emoji in modern terminals
      (cp >= 0x2600 && cp <= 0x26ff) ||
      // Dingbats (✨ ✅ ❌ etc.)
      (cp >= 0x2700 && cp <= 0x27bf) ||
      // CJK Radicals Supplement .. CJK Symbols and Punctuation
      (cp >= 0x2e80 && cp <= 0x303e) ||
      // Hiragana .. CJK Unified Ideographs Extension A
      (cp >= 0x3040 && cp <= 0x4dbf) ||
      // CJK Unified Ideographs .. Yi Radicals
      (cp >= 0x4e00 && cp <= 0xa4cf) ||
      // Hangul Jamo Extended-A
      (cp >= 0xa960 && cp <= 0xa97f) ||
      // Hangul Syllables .. Hangul Jamo Extended-B
      (cp >= 0xac00 && cp <= 0xd7ff) ||
      // CJK Compatibility Ideographs
      (cp >= 0xf900 && cp <= 0xfaff) ||
      // Vertical Forms
      (cp >= 0xfe10 && cp <= 0xfe19) ||
      // CJK Compatibility Forms .. Small Form Variants
      (cp >= 0xfe30 && cp <= 0xfe6f) ||
      // Fullwidth Latin, punctuation, and signs
      (cp >= 0xff00 && cp <= 0xff60) ||
      (cp >= 0xffe0 && cp <= 0xffe6) ||
      // Kana Supplement / Extended-A / Extended-B
      (cp >= 0x1b000 && cp <= 0x1b2ff) ||
      // Enclosed Ideographic Supplement
      (cp >= 0x1f004 && cp <= 0x1f004) ||
      (cp >= 0x1f0cf && cp <= 0x1f0cf) ||
      (cp >= 0x1f18b && cp <= 0x1f251) ||
      // Miscellaneous Symbols and Pictographs .. Supplemental Symbols and
      // Pictographs (includes Geometric Shapes Extended U+1F780–U+1F7FF,
      // where coloured circle emoji such as 🟢 U+1F7E2 live)
      (cp >= 0x1f300 && cp <= 0x1faff) ||
      // CJK Unified Ideographs Extension B and beyond
      (cp >= 0x20000 && cp <= 0x2fffd) ||
      // CJK Unified Ideographs Extension G and beyond
      (cp >= 0x30000 && cp <= 0x3fffd)
