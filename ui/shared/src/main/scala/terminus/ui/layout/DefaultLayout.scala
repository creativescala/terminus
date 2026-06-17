package terminus.ui.layout

import terminus.ui.capability.Layout
import terminus.ui.runtime.Runtime
import scala.collection.mutable

/** The default implementation of [[terminus.ui.capability.Layout]]. */
trait DefaultLayout(runtime: Runtime) extends Layout:
  private[ui] val components: mutable.ArrayBuffer[Component] =
    mutable.ArrayBuffer.empty

  def addComponent(build: Runtime => Component): Unit =
    components += build(runtime)
object DefaultLayout:
  def apply(runtime: Runtime): DefaultLayout =
    new DefaultLayout(runtime) {}
