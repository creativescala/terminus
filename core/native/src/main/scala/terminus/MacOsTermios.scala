package terminus

import scalanative.unsafe.*
import scalanative.posix

/** macOS implementation of Termios, which just uses the Scala Native implementation */
object MacOsTermios extends Termios {
  type Attributes = Ptr[posix.termios.termios]

  private val STDIN = scala.scalanative.posix.unistd.STDIN_FILENO

  def getAttributes()(using Zone): Attributes = {
    val attrs: Attributes = alloc[posix.termios.termios]()
    posix.termios.tcgetattr(STDIN, attrs)
    attrs
  }

  def setRawMode(): Unit = {
    Zone {
      val attrs = getAttributes()
      attrs._4 = attrs._4 & ~(posix.termios.ECHO | posix.termios.ICANON)
      setAttributes(attrs)
    }
  }

  def setAttributes(attributes: Attributes): Unit = {
    val _ = posix.termios.tcsetattr(STDIN, posix.termios.TCSAFLUSH, attributes)
    ()
  }
}
