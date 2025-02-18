package terminus

import scala.scalanative.posix
import scala.scalanative.unsafe.{CInt, CStruct7}

object TermiosStruct {
  private type linux_tcflag_t = CInt // Changed
  private type linux_speed_t = CInt // Changed

  type cint_flags = CStruct7[
    linux_tcflag_t,     /* c_iflag - input flags   */
    linux_tcflag_t,     /* c_oflag - output flags  */
    linux_tcflag_t,     /* c_cflag - control flags */
    linux_tcflag_t,     /* c_lflag - local flags   */
    posix.termios.c_cc, /* cc_t c_cc[NCCS] - control chars */
    linux_speed_t,      /* c_ispeed - input speed   */
    linux_speed_t       /* c_ospeed - output speed  */
  ]

  type clong_flags = posix.termios.termios
}