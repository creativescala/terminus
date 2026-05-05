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

import scala.scalanative.unsafe.*

/** The C `struct winsize` used with `ioctl` to query and set terminal
  * dimensions. Fields, in order: ws_row, ws_col, ws_xpixel, ws_ypixel.
  */
type WinSize =
  CStruct4[CUnsignedShort, CUnsignedShort, CUnsignedShort, CUnsignedShort]

/** Extern bindings for the TIOCGWINSZ and TIOCSWINSZ ioctl request constants.
  * These are C preprocessor macros so they are exposed via thin C helper
  * functions defined in winsize.c.
  */
@extern
private[terminus] object winsizeConstants:
  @name("terminus_tiocgwinsz")
  def TIOCGWINSZ: CLongInt = extern

  @name("terminus_tiocswinsz")
  def TIOCSWINSZ: CLongInt = extern
