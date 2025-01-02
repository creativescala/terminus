package terminus

import scala.scalajs.js

@js.native
trait XtermJsOptions extends js.Object {
  val cols: Int = js.native
  val rows: Int = js.native
}
object XtermJsOptions {
  def apply(rows: Int = 80, cols: Int = 24): XtermJsOptions =
    js.Dynamic.literal(rows = rows, cols = cols).asInstanceOf[XtermJsOptions]
}
