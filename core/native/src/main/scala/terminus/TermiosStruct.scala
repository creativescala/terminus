package terminus

import scala.scalanative.posix
import scala.scalanative.unsafe.{CInt, CStruct7}

/** Type aliases for the two possible termios structures, one with CInt bitflags and one with CLong bitflags.
  * CLong types are the default in scala-native, however linux (and probably more operating systems) use CInt
  * bitflags. Each of these types has a [[terminus.TermiosAccess]] instance to handle access and manipulation
  * of termios structures in a platform specific manner.
  *
  * @see [[https://pubs.opengroup.org/onlinepubs/7908799/xsh/termios.h.html POSIX `termios.h` Documentation]]
  * @see [[https://github.com/apple/darwin-xnu/blob/main/bsd/sys/termios.h#L263-L265 Darwin termios.h types]]
  * @see [[https://github.com/scala-native/scala-native/issues/4143 Scala Native termios linux issue]]
  */
object TermiosStruct {
  // Custom flag types using CInt
  private type linux_tcflag_t = CInt
  private type linux_speed_t = CInt

  /** Custom `termios` structure with CInt sized bitflags */
  type cint_flags = CStruct7[
    linux_tcflag_t,     /* c_iflag - input flags   */
    linux_tcflag_t,     /* c_oflag - output flags  */
    linux_tcflag_t,     /* c_cflag - control flags */
    linux_tcflag_t,     /* c_lflag - local flags   */
    posix.termios.c_cc, /* cc_t c_cc[NCCS] - control chars */
    linux_speed_t,      /* c_ispeed - input speed   */
    linux_speed_t       /* c_ospeed - output speed  */
  ]

  /** Scala Native default [[posix.termios.termios termios]] structure with CLong sized bitflags */
  type clong_flags = posix.termios.termios
}
