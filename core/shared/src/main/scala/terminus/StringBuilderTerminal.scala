package terminus

import scala.collection.mutable

final class StringBuilderTerminal()
    extends effect.Color[StringBuilderTerminal],
      effect.Cursor,
      effect.Display[StringBuilderTerminal],
      effect.Erase,
      effect.Mode[StringBuilderTerminal],
      effect.Writer {

        private val builder = mutable.StringBuilder()


        def alternateScreen[A](f: (terminus.StringBuilderTerminal) ?=> A): A = ???
        def application[A](f: (terminus.StringBuilderTerminal) ?=> A): A = ???
        def raw[A](f: (terminus.StringBuilderTerminal) ?=> A): A = ???

        /** Flush is a no-op */
        def flush(): Unit = ()

        def write(char: Char): Unit =
          builder.addOne(char)

        def write(string: String): Unit =
          builder.addAll(string)

        /** Get the value accumulated in the internal string builder, clearing the
          * buffer in the process. */
        def result(): String = {
          val s = builder.result()
          builder.clear()
          s
        }
      }
object StringBuilderTerminal {
  type Program[A] = StringBuilderTerminal ?=> A

  def run[A](f: Program[A]): String = {
    val terminal = StringBuilderTerminal()
    val _ = f(using terminal)

    terminal.result()
  }
}
