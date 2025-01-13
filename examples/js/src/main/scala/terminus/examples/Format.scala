package terminus.examples

import terminus.*
import scala.scalajs.js.annotation.*

@JSExportTopLevel("Format")
object Format {
  @JSExport
  def go(id: String) =
    Terminal.run(id, rows = 3) {
      Terminal.display.bold(
        Terminal.display.strikethrough(
          Terminal.write("Bold and strikethrough\r\n")
        )
      )
      Terminal.foreground.white(
        Terminal.background.red(
          Terminal.write("Foreground and background color\r\n")
        )
      )
      Terminal.display.invert(
        Terminal.display.underline.curly(
          Terminal.write("Inverted with curly underline")
        )
      )
      Terminal.flush()
    }
}
