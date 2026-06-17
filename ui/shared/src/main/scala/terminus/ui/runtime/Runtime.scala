package terminus.ui.runtime

import terminus.Key
import terminus.ui.event.FocusId

import scala.collection.mutable

/** The runtime provides capabilties that are internal to the system and not
  * used by the application programmer.
  */
final class Runtime private ():
  private var currentFocus: FocusId = FocusId.zero
  private var focusListIdx: Int = 0

  // The root handlers gets to handle events before the focused element. If they
  // handle an event it is *not* passed to the focused element.
  private var rootHandlers: Map[Key, Seq[() => Unit]] =
    Map.empty

  // The order in which we visit focusables. Follows the order in which they are
  // added.
  private val focusablesOrder: mutable.ArrayBuffer[FocusId] =
    mutable.ArrayBuffer.empty

  private val focusables: mutable.Map[FocusId, Runtime.Focusable] =
    mutable.Map.empty

  def currentFocusId: FocusId = currentFocus

  def addRootHandlers(
      handlers: Map[Key, Seq[() => Unit]]
  ): Unit =
    rootHandlers = handlers

  def addKeyHandler(
      focusId: FocusId,
      key: Key,
      handler: () => Unit
  ): Unit =
    focusables
      .getOrElseUpdate(focusId, Runtime.Focusable.empty)
      .addKeyHandler(key, handler)

  def addAnyKeyHandler(
      focusId: FocusId,
      handler: Key => Unit
  ): Unit =
    focusables
      .getOrElseUpdate(focusId, Runtime.Focusable.empty)
      .addAnyKeyHandler(handler)

  def nextFocus(): Unit =
    if focusablesOrder.size == 0 then ()
    else
      focusListIdx = (focusListIdx + 1) % focusablesOrder.size
      currentFocus = focusablesOrder(focusListIdx)

  def prevFocus(): Unit =
    if focusablesOrder.size == 0 then ()
    else
      focusListIdx =
        if focusListIdx == 0 then focusablesOrder.size - 1 else focusListIdx - 1
      currentFocus = focusablesOrder(focusListIdx)

  def dispatch(key: Key): Unit =
    rootHandlers.get(key) match
      case None =>
        focusables.get(currentFocus) match
          case None            => ()
          case Some(focusable) => focusable.handle(key)
      case Some(handlers) => handlers.foreach(f => f())

object Runtime:
  case class Focusable(
      keyHandlers: mutable.Map[Key, mutable.ArrayBuffer[() => Unit]],
      anyKeyHandlers: mutable.ArrayBuffer[Key => Unit]
  ):
    def addKeyHandler(key: Key, handler: () => Unit): Unit =
      keyHandlers.getOrElseUpdate(key, mutable.ArrayBuffer.empty) += handler

    def addAnyKeyHandler(handler: Key => Unit): Unit =
      anyKeyHandlers += handler

    def handle(key: Key): Unit =
      keyHandlers.get(key) match
        case None           => ()
        case Some(handlers) => handlers.foreach(f => f())
      anyKeyHandlers.foreach(f => f(key))

  object Focusable:
    val empty: Focusable =
      Focusable(mutable.Map.empty, mutable.ArrayBuffer.empty)

  def empty: Runtime = new Runtime()
