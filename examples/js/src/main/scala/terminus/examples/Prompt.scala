package terminus.examples

import terminus.*
import scala.scalajs.js.annotation.*
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@JSExportTopLevel("Prompt")
object Prompt {
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
      Terminal.format.bold(Terminal.write(s"> ${description}\r\n"))
    else Terminal.write(s"  ${description}\r\n")

  // Write the UI
  def write(selected: Int): Program[Unit] = {
    Terminal.write("How cool is this?\r\n")
    writeChoice("Very cool", selected == 0)
    writeChoice("Way cool", selected == 1)
    writeChoice("So cool", selected == 2)
    Terminal.flush()
  }

  // Convert Javascript key to KeyCode
  // See https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/key
  def read(): Program[Future[KeyCode]] = {
    Terminal.readKey().flatMap( keyCode =>
      keyCode match {
        case "Enter" => Future.successful(KeyCode.Enter)
        case "ArrowDown" => Future.successful(KeyCode.Down)
        case "ArrowUp" => Future.successful(KeyCode.Up)
        case other => read()
      }
    )
  }

  def loop(idx: Int): Program[Future[Int]] = {
    write(idx)
      read().flatMap(keyCode =>
        keyCode match {
          case KeyCode.Up =>
            clear()
            loop(if idx == 0 then 2 else idx - 1)

          case KeyCode.Down =>
            clear()
            loop(if idx == 2 then 0 else idx + 1)

          case KeyCode.Enter => Future.successful(idx)
        }
      )
    }
  @JSExport
  def go(id: String) =
    Terminal.run(id, rows = 16) {
      loop(0).map(idx =>
        Terminal.write(s"You selected $idx")
      )
    }
}
