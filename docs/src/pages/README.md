# Terminus

Terminus is a Scala 3 library for working with the terminal.
It currently supports JVM, Scala Native, and Javascript backends.


## Setup

To use Terminus, add the following to your `build.sbt`

```scala
libraryDependencies += "org.creativescala" %% "terminus-core" % "@VERSION@"
```


## Usage

Import Terminus

```scala mdoc:silent
import terminus.*
```

Now you can call methods on the `Terminal` object. The core methods are `read` and `write`, but there are also methods to change color, move the cursor, erase content, and so on. On most terminals you will need to call `flush` or your output won't appear. Wrap a call to `run` around your entire program. Here's a small example that prints green text.


```scala mdoc:compile-only
Terminal.run {
  Terminal.display.bold {
    Terminal.foreground.green {
      Terminal.write("This is Terminus!")
      Terminal.flush()
    }
  }
}
```

This produces the following output.

@:doodle("color-foreground-green", "ColorForegroundGreen.go")

See the [Examples](examples.md) for more involved use cases.


## Design

The API in the `terminus` package provides a functional API for working with the terminal.
Methods consume and return context functions with type

```scala
type Program[A] = Terminal ?=> A
```

where `Terminal` is a backend specific implementation that handles interfacing with the terminal. A `Program` represents a function that, when run, will do something with the terminal.

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

If you want to work directly with the terminal, without working with context functions, you can work with the types in `terminus.effect`.


## Low-level Code

All the ANSI escape codes used by Terminus are defined in `terminus.effect.AnsiCodes`.
This can be useful if you want to write [escape codes][ansi-escape-codes] directly to the terminal without the abstractions provided by the Terminus DSL.
Here's a simple example.

```scala mdoc
import terminus.effect.AnsiCodes

AnsiCodes.foreground.red
AnsiCodes.erase.line
```


## Notes

You won't be able to run terminal programs from sbt if the JVM forks when running. That is, if you have the setting

```scala
run / fork := true
```

the forked JVM won't have access to a terminal and therefore any terminal programs will not work as expected.


## Related Work

[Fansi][fansi] may be a better choice if your only interest in styling output printed to a terminal.

[Cue4s][cue4s] provides higher level abstractions for building UIs in the terminal.


## Further Reading

The terminal is much more evolved than designed. I haven't been able to find a single document that describes all the features. Here are a few resources that I've found useful for piecing together how things should work:

- Wikipedia has reasonable documentation of the [ANSI escape codes][ansi-escape-codes], though it doesn't cover semantics in detail (e.g. erasing seems to move the cursor but Wikipedia doesn't describe this in all cases.)

- [This article](http://web.archive.org/web/20160407191115/http://homes.mpimf-heidelberg.mpg.de/~rohm/computing/mpimf/notes/terminal.html) describes a bit about cursor and application mode.

- [The Kitty Keyboard Protocol](https://sw.kovidgoyal.net/kitty/keyboard-protocol/) describes the goofiness in the basic terminal key handling and also describes a fix.

- [A state machine for parsing terminal input](https://vt100.net/emu/dec_ansi_parser) will also be handy.

[ansi-escape-codes]: https://en.wikipedia.org/wiki/ANSI_escape_code
[jline]: https://jline.org/
[scala-native]: https://scala-native.org/en/stable/
[scala-js]: https://www.scala-js.org/
[fansi]: https://github.com/com-lihaoyi/fansi
[cue4s]: https://github.com/neandertech/cue4s/
