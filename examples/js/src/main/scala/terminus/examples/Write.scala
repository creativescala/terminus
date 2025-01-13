package terminus.examples

import terminus.*
import scala.scalajs.js.annotation.*

@JSExportTopLevel("Write")
object Write {
  @JSExport
  def go(id: String) =
    Terminal.run(id, rows = 3) {
      Terminal.write(
        "“Either write things worth reading or do things worth the writing.”\r\n  -Benjamin Franklin"
      )
      Terminal.flush()
    }
}
