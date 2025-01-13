# Guide

## Imports

The standard import is just

```scala mdoc
import terminus.*
```

This will import

1. the `Terminal` object, which contains all the methods you'll use;
2. the `Terminal` type, which represents a backend-specific implementation; and
3. the `Program` type, described immediately below.


## Programs

In Terminus you construct a program that says what you want to do with the terminal, and then `run` that program when it is fully constructed. This is similar to using the `IO` monad, where you construct the monad stating what you want to do and then call one of the `unsafe` methods to carry out those actions.

In Terminus program is basically a function that accepts some state representing the terminal and returns a value of type `A`. More concretely it is

```scala
type Program[A] = Terminal ?=> A
```

where `Terminal` is a backend specific implementation that handles interfacing with the terminal on that platform.


## Working with Context Functions

Context functions are new to Scala 3, so many developers may not be familiar with how they work. There are three rules for working with them, described below.

The first rule is that if the compiler can tell that a context function is expected it will automatically create one. We can do this with a `Program` type annotation. Here are some examples

```scala mdoc:compile-only
// Most of the methods on Terminal return programs
val aTerminalOperation: Program[Unit] = Terminal.write("Some text")

// Any expression can be a Program with a type annotation
val aProgram: Program[Int] = 1 + 1
```

The second rule is that context functions will be automatically applied if there is a `given` value of the appropriate type in scope. This is what allows us to write effectful code in so-called *direct-style*, which just means writing normal code without monads or other complications. Here's an example that mixes effectful code, using the terminal, with some normal code. Notice that the entire result is a `Program`. This type annotation means the compiler constructs a context function around the entire block.

```scala mdoc:compile-only
val writeSomeStuff: Program[Int] = {
  Terminal.write("Some output")
  // We can mix normal code in
  val result = 1 + 1
  Terminal.write("More output")
  Terminal.flush()
  result
}
```
  
We can do the same thing with a method, by specifying the return type is a `Program`. Here's an example.
  
```scala mdoc:silent
def doSomeStuff(): Program[Int] = {
  Terminal.write("Some output")
  val result = 1 + 1
  Terminal.write("More output")
  Terminal.flush()
  result
}
```

The final rule is if we don't tell the compiler we're expecting a context function, we may get an error when the compiler attempts to apply a context function to a given value that does not exist.

```scala mdoc:fail
Terminal.write("Some output")
```

We can solve this by either adding a context function type annotation

```scala mdoc:compile-only
val ok: Program[Unit] = Terminal.write("Some output")
```

or by providing a `given` value of the required type, which is what `Terminal.run` does.

```scala mdoc:compile-only
Terminal.run(Terminal.write("Some output"))
```


## Building Terminal Programs

In Terminus you build programs by calling methods on the `Terminal` object. This section describes the most important methods. See the API @:api(terminus.index) for more.


### Writing to the Terminal

Call the `write` method to send a `String` or `Char` to the terminal. On most terminals you'll need to call `flush` before your output actually appears.

```scala mdoc:compile-only
Terminal.run {
  Terminal.write("“Either write things worth reading or do things worth the writing.”\r\n  -Benjamin Franklin")
  Terminal.flush()
}
```

This produces the output shown below. Note that the terminal does not follow the Unix convention and interpret a newline (`\n`) as both a carriage return (`\r`) and a newline. We must specify both if we want to move down a line and return to the leftmost column.

@:doodle("write", "Write.go")

### Reading from the Terminal

Call `read` to wait for a character from the terminal. This returns character codes directly as sent by the terminal, which uses escape codes to encode various keys. For example, the up arrow is represented as the sequence of characters `ESC`, `[`, and `A`. You'll have to, for now, parse these yourself.


### Formatting Output

Terminus provides many methods to control the formatting of text in the terminal. You can set the foreground or background, make text bold, make text blink, and a lot more. See the @:api(terminus.effect.Display) and @:api(terminus.effect.Color) API documentation to see everything that is available.

These methods all take a `Program` as a parameter. The effect is applied to any output from that `Program` program, but it not persist outside the scope of the method. These methods can be arbitrarily nested, and will work correctly. For example, we can nest green text inside yellow text and get the correct mix of yellow and green.

```scala mdoc:compile-only
Terminal.run {
  Terminal.foreground.yellow {
    Terminal.write("Yellow ")
    Terminal.foreground.green(Terminal.write("Green "))
    Terminal.write("Yellow ")
  }
  Terminal.write("Unstyled")
  Terminal.flush()
}
```

@:doodle("nested-format", "NestedFormat.go")

This example shows some more of the formatting options available.

```scala mdoc:compile-only
Terminal.run {
  Terminal.display.bold(
    Terminal.display.strikethrough(
      Terminal.write("Bold and strikethrough\r\n")
    )
  )
  Terminal.foreground.white(
    Terminal.background.red(
      Terminal.write("Foreground and background color\r\n")
    )
  )
  Terminal.display.invert(
    Terminal.display.underline.curly(
      Terminal.write("Inverted with curly underline")
    )
  )
  Terminal.flush()
}
```

@:doodle("format", "Format.go")


### Running Programs

Use `Terminal.run`, as shown in the examples above, to run a `Program` and cause it to affect the terminal. This method is backend dependent. For example, on the Javascript backend it allows you to specify additional parameters that change how the terminal is rendered in the browser.
