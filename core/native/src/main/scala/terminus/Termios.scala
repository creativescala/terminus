package terminus

import scala.scalanative.unsafe.Zone


/** An abstraction of the termios library that only exposes the functionality we need */
trait Termios {
  /** The terminal attributes data structure. (Called termios in the POSIX API.) */
  type Attributes

  def getAttributes()(using Zone): Attributes
  def setRawMode(): Unit
  def setAttributes(attributes: Attributes): Unit
}
