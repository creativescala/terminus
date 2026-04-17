/*
 * Copyright 2024 Creative Scala
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package terminus.ui

import terminus.AlternateScreenMode
import terminus.Cursor
import terminus.Erase
import terminus.Key
import terminus.KeyReader
import terminus.RawMode
import terminus.Writer
import terminus.effect

import scala.collection.mutable

/** A RootContext that acts as a column and renders into a cell buffer. */
class FullScreen() extends RootContext:
  private val children: mutable.ArrayBuffer[Component] =
    mutable.ArrayBuffer.empty

  def size: Size =
    children.foldLeft(Size.zero)((acc, c) => acc.column(c.size))

  def add(component: Component): Unit =
    children += component

  private[ui] def toBuffer(): Buffer =
    val currentSize = size
    val buf = Buffer(currentSize.width, currentSize.height)
    var y = 0
    children.foreach { child =>
      val childSize = child.size
      child.render(Rect(0, y, childSize.width, childSize.height), buf)
      y += childSize.height
    }
    buf

  def render(using Terminal): Unit =
    toBuffer().render

object FullScreen:
  type Terminal = effect.AlternateScreenMode & effect.Erase &
    terminus.ui.Terminal
  type Program[A] = FullScreen.Terminal ?=> A

  type InteractiveTerminal =
    FullScreen.Terminal & effect.KeyReader & effect.RawMode
  type InteractiveProgram[A] = InteractiveTerminal ?=> A

  object Terminal extends AlternateScreenMode, Cursor, Erase, Writer
  object InteractiveTerminal
      extends AlternateScreenMode,
        Cursor,
        Erase,
        KeyReader,
        RawMode,
        Writer

  def apply[A](f: AppContent[A]): FullScreen.Program[A] =
    val fullScreen = new FullScreen()
    given AppContext = noopAppContext(fullScreen)
    val result = f
    FullScreen.Terminal.erase.screen()
    fullScreen.render
    result

  /** Run an interactive event loop.
    *
    * `f` is called once to build the component tree and register event
    * handlers. The tree is then rendered, and the loop blocks on key input,
    * dispatching to registered handlers and re-rendering whenever a signal
    * changes. The loop exits when [[EventContext.stop]] is called or stdin
    * reaches EOF.
    */
  def run(f: AppContent[Unit]): InteractiveProgram[Unit] = t ?=>
    val ec = new EventContextImpl()
    val fullScreen = new FullScreen()

    given AppContext = new AppContext:
      private[ui] def invalidate(): Unit = ec.scheduleRerender()
      def createSignal[A](initial: A): Signal[A] = ec.createSignal(initial)
      def onKey(key: Key)(handler: => Unit): Unit = ec.onKey(key)(handler)
      def stop(): Unit = ec.stop()
      def size: Size = fullScreen.size
      def add(component: Component): Unit = fullScreen.add(component)

    f // build component tree and register handlers

    InteractiveTerminal.cursor.hidden {
      InteractiveTerminal.raw {
        InteractiveTerminal.erase.screen()

        var prevBuffer: Option[Buffer] = None

        def renderFrame(): Unit =
          val buf = fullScreen.toBuffer()
          prevBuffer match
            case None    => buf.render
            case Some(p) => buf.renderDiff(p)
          prevBuffer = Some(buf)
          ec.clearRerender()

        renderFrame()

        while ec.running do
          InteractiveTerminal.readKey() match
            case terminus.Eof => ec.stop()
            case key: Key     =>
              ec.dispatch(key)
              if ec.needsRerender then renderFrame()
      }
    }

  /** A no-op AppContext for non-interactive (single-render) use. */
  private def noopAppContext(fullScreen: FullScreen): AppContext =
    new AppContext:
      private[ui] def invalidate(): Unit = ()
      def createSignal[A](initial: A): Signal[A] = new Signal[A]:
        private var value = initial
        def get: A = value
        def set(a: A): Unit = value = a
      def onKey(key: Key)(handler: => Unit): Unit = ()
      def stop(): Unit = ()
      def size: Size = fullScreen.size
      def add(component: Component): Unit = fullScreen.add(component)
