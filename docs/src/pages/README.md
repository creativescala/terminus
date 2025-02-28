# Terminus

Terminus is a Scala 3 library for working with the terminal.
Using Terminus you can build:

- terminal applications that use the full ecosystem of the JVM;
- small and fast binaries using Scala Native; and
- terminal application that run in the browser using Scala.js.


## Installation

To use Terminus, add the following to your `build.sbt`

```scala
libraryDependencies += "org.creativescala" %%% "terminus-core" % "@VERSION@"
```


## Usage

Import Terminus

```scala mdoc:silent
import terminus.*
```

Now call methods on the `Terminal` object. The core methods are `read` and `write`, but there are also methods to change color, move the cursor, erase content, and so on. On most terminals you will need to call `flush` or your output won't appear. Wrap a call to `run` around your entire program. Here's a small example that prints green text.


```scala mdoc:compile-only
Terminal.run {
  Terminal.format.bold {
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
