package terminus.effect

/** Ascii character codes and Ascii related extension methods. See:
  * https://www.ascii-code.com/
  */
object Ascii {
  
  /** Null character */
  val NUL: Char = '\u0000'
  
  /** Start of Heading */
  val SOH: Char = '\u0001'
  
  /** Start of Text */
  val STX: Char = '\u0002'
  
  /** End of Text */
  val ETX: Char = '\u0003'
  
  /** End of Transmission */
  val EOT: Char = '\u0004'
  
  /** Enquiry */
  val ENQ: Char = '\u0005'
  
  /** Acknowledge */
  val ACK: Char = '\u0006'
  
  /** Bell, Alert */
  val BEL: Char = '\u0007'
  
  /** Backspace */
  val BS: Char = '\u0008'
  
  /** Horizontal Tab */
  val HT: Char = '\u0009'
  
  /** Line Feed */
  val LF: Char = '\u000A'
  
  /** Vertical Tabulation */
  val VT: Char = '\u000B'
  
  /** Form Feed */
  val FF: Char = '\u000C'
  
  /** Carriage Return */
  val CR: Char = '\u000D'
  
  /** Shift Out */
  val SO: Char = '\u000E'
  
  /** Shift In */
  val SI: Char = '\u000F'
  
  /** Data Link Escape */
  val DLE: Char = '\u0010'
  
  /** Device Control One (XON) */
  val DC1: Char = '\u0011'
  
  /** Device Control Two */
  val DC2: Char = '\u0012'
  
  /** Device Control Three (XOFF) */
  val DC3: Char = '\u0013'
  
  /** Device Control Four */
  val DC4: Char = '\u0014'
  
  /** Negative Acknowledge */
  val NAK: Char = '\u0015'
  
  /** Synchronous Idle */
  val SYN: Char = '\u0016'
  
  /** End of Transmission Block */
  val ETB: Char = '\u0017'
  
  /** Cancel */
  val CAN: Char = '\u0018'
  
  /** End of medium */
  val EM: Char = '\u0019'
  
  /** Substitute */
  val SUB: Char = '\u001A'
  
  /** Escape */
  val ESC: Char = '\u001B'
  
  /** File Separator */
  val FS: Char = '\u001C'
  
  /** Group Separator */
  val GS: Char = '\u001D'
  
  /** Record Separator */
  val RS: Char = '\u001E'
  
  /** Unit Separator */
  val US: Char = '\u001F'

  /** Delete */
  val DEL: Char = '\u007F'

  extension (char: Char)
    def isControl: Boolean = char >= 0x00 && char < 0x20
    def isPrintable: Boolean = char >= 0x20 && char != DEL
}
