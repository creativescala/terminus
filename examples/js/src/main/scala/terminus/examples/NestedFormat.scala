package terminus.examples

import terminus.*
import scala.scalajs.js.annotation.*

@JSExportTopLevel("NestedFormat")
object NestedFormat {
  @JSExport
  def go(id: String) =
    Terminal.run(id, rows = 3) {
      Terminal.foreground.yellow {
        Terminal.write("Yellow ")
        Terminal.foreground.green(Terminal.write("Green "))
        Terminal.write("Yellow ")
      }
      Terminal.write("Unstyled")
      Terminal.flush()
    }
}
