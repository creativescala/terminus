package terminus.ui.event

import terminus.Key
import terminus.ui.capability.Event
import terminus.ui.runtime.Runtime

/** The default implementation of the [[terminus.ui.capability.Event]]
  * capability, which defers to [[terminus.ui.runtime.Runtime]] for its
  * implementation.
  */
trait DefaultEvent(focusId: FocusId, runtime: Runtime) extends Event:

  def onKey(key: Key)(handler: => Unit): Unit =
    runtime.addKeyHandler(focusId, key, () => handler)

  def onAnyKey(handler: Key => Unit): Unit =
    runtime.addAnyKeyHandler(focusId, handler)

  def nextFocus(): Unit = runtime.nextFocus()

  def prevFocus(): Unit = runtime.prevFocus()

  def hasFocus: Boolean = runtime.currentFocusId == focusId
