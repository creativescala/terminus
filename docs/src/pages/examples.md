# Examples

## Prompt

This example shows a simple UI you can easily build with Terminus. This is the kind of thing you might create for a command line tool. Make sure the emulated terminal has focus (i.e. click in the black rectangle) and then use up and down arrows to select an option. Reload the page to start again.

@:doodle("terminus-prompt", "Prompt.go")

Here's the code for this example, running on the JVM backend. (The JS code is slightly different due to differences in how input is handled.)

```scala 3
import terminus.effect.{ Ascii, Eof }

enum KeyCode {
  case Down
  case Up
  case Enter
}

// Clear the text we've written
def clear(): Program[Unit] = {
  Terminal.cursor.move(1, -4)
  Terminal.erase.down()
  Terminal.cursor.column(1)
}

// Write an option the user can choose. The currently selected option is highlighted.
def writeChoice(description: String, selected: Boolean): Program[Unit] =
  if selected then
    Terminal.display.bold(Terminal.write(s"> ${description}\r\n"))
  else Terminal.write(s"  ${description}\r\n")

// Write the UI
def write(selected: Int): Program[Unit] = {
  Terminal.write("How cool is this?\r\n")
  writeChoice("Very cool", selected == 0)
  writeChoice("Way cool", selected == 1)
  writeChoice("So cool", selected == 2)
  Terminal.flush()
}

@tailrec
def read(): Program[KeyCode] = {
  Terminal.read() match {
    case Eof =>
      throw new Exception("Received an EOF")
    case char: Char =>
      char match {
        case Ascii.LF | Ascii.CR => KeyCode.Enter
        case Ascii.ESC =>
          Terminal.read() match {
            // Normal mode
            case '[' =>
              Terminal.read() match {
                case 'A'   => KeyCode.Up
                case 'B'   => KeyCode.Down
                case other => read()
              }

            // Application mode
            case 'O' =>
              Terminal.read() match {
                case 'A'   => KeyCode.Up
                case 'B'   => KeyCode.Down
                case other => read()
              }

            case other => read()
          }
        case other => read()
      }
  }
}

@tailrec
def loop(idx: Int): Program[Int] = {
  write(idx)
  read() match {
    case KeyCode.Up =>
      clear()
      loop(if idx == 0 then 2 else idx - 1)

    case KeyCode.Down =>
      clear()
      loop(if idx == 2 then 0 else idx + 1)

    case KeyCode.Enter => idx
  }
}

@main def prompt(): Unit = {
  val idx =
    Terminal.run(
      Terminal.raw { loop(0) }
    )

  println(s"Selected $idx")
}
```
