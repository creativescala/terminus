package terminus

import scala.scalanative.posix
import scala.scalanative.unsafe._

trait TermiosAccess[T] {
  val STDIN = scala.scalanative.posix.unistd.STDIN_FILENO

  type FlagType <: CInt | CLong

  def get(using Zone): Ptr[T]
  def set(ptr: Ptr[T]): Unit

  def addLocalFlags(attrs: Ptr[T], flags: CInt): Unit
  def removeLocalFlags(attrs: Ptr[T], flags: CInt): Unit
}

extension [T](ptr: Ptr[T])(using au: TermiosAccess[T]) {
  def addLocalFlags(flags: CInt): Unit = au.addLocalFlags(ptr, flags)
  def removeLocalFlags(flags: CInt): Unit = au.removeLocalFlags(ptr, flags)
}

given macTermiosAccess: TermiosAccess[TermiosTypes.mac] =
  new TermiosAccess[TermiosTypes.mac] {
    override type FlagType = CLong

    override def get(using Zone): Ptr[TermiosTypes.mac] = {
      val attrs: Ptr[TermiosTypes.mac] = alloc[TermiosTypes.mac]()
      posix.termios.tcgetattr(STDIN, attrs)
      attrs
    }

    override def set(ptr: Ptr[TermiosTypes.mac]): Unit = {
      val _ = posix.termios.tcsetattr(STDIN, posix.termios.TCSAFLUSH, ptr)
      ()
    }

    def addLocalFlags(attrs: Ptr[TermiosTypes.mac], flags: CInt): Unit =
      attrs._4 = attrs._4 | flags

    def removeLocalFlags(attrs: Ptr[TermiosTypes.mac], flags: CInt): Unit =
      attrs._4 = attrs._4 & ~flags
  }

object TermiosTypes {
  private type linux_tcflag_t = CInt // Changed
  private type linux_speed_t = CInt // Changed

  type linux = CStruct7[
    linux_tcflag_t, /* c_iflag - input flags   */
    linux_tcflag_t, /* c_oflag - output flags  */
    linux_tcflag_t, /* c_cflag - control flags */
    linux_tcflag_t, /* c_lflag - local flags   */
    posix.termios.c_cc, /* cc_t c_cc[NCCS] - control chars */
    linux_speed_t, /* c_ispeed - input speed   */
    linux_speed_t /* c_ospeed - output speed  */
  ]

  type mac = posix.termios.termios
}

given linuxTermiosAccess: TermiosAccess[TermiosTypes.linux] =
  new TermiosAccess[TermiosTypes.linux] {
    override type FlagType = CInt

    override def get(using Zone): Ptr[TermiosTypes.linux] = {
      val attrs: Ptr[TermiosTypes.linux] = alloc[TermiosTypes.linux]()
      tcgetattr(STDIN, attrs)
      attrs
    }

    override def set(ptr: Ptr[TermiosTypes.linux]): Unit = {
      val _ = tcsetattr(STDIN, posix.termios.TCSAFLUSH, ptr)
      ()
    }

    override def addLocalFlags(
                                attrs: Ptr[TermiosTypes.linux],
                                flags: CInt
                              ): Unit = attrs._4 = attrs._4 | flags

    override def removeLocalFlags(
                                   attrs: Ptr[TermiosTypes.linux],
                                   flags: CInt
                                 ): Unit = attrs._4 = attrs._4 & ~flags

    @extern
    def tcgetattr(fd: CInt, termios_p: Ptr[TermiosTypes.linux]): CInt = extern

    @extern
    def tcsetattr(
                   fd: CInt,
                   optionalActions: CInt,
                   termios_p: Ptr[TermiosTypes.linux]
                 ): CInt = extern
  }