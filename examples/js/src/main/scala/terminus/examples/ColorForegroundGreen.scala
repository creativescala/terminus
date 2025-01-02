package terminus.examples

import terminus.*
import scala.scalajs.js.annotation.*

@JSExportTopLevel("ColorForegroundGreen")
object ColorForegroundGreen {
  @JSExport
  def go(id: String) =
    Terminal.run(id, rows = 3) {
      Terminal.display.bold {
        Terminal.foreground.green {
          Terminal.write("This is Terminus!")
          Terminal.flush()
        }
      }
    }
}
