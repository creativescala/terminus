package terminus.ui.react

import terminus.ui.capability.React

import scala.collection.mutable

trait DefaultReact extends React:
  def stack: mutable.Stack[Listener] = mutable.Stack.empty
object DefaultReact:
  def empty: React = new DefaultReact()
