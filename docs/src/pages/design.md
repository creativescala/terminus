# Design

A Terminus program consists of two parts: the `Program` that describes what we want to do, and the interpreter that carries out those actions. Terminus user spend their time creating `Programs`, but don't think much about how the interpreter works. In this section we describe the implementation of the interpreters and the `Programs` that use them.

## Programs and Effects

A `Program` is a [context function][context-function] with type `Terminal ?=> A`, where `Terminal` is some backend specific type. The `Terminal` type is backend specific because different terminals support different features. For example, the Javascript xterm.js doesn't have a concept of raw mode because it doesn't intercept key presses in the first place; this is something that is managed by the browser.

When we call `Terminal.run` we carry out the description in the `Program`. This is the interpreter. It is split into two parts: function application, which is just passing a concrete `Terminal` instance to the `Program`, and the effects that are defined in that `Terminal` instance.


## Implementing Effects

We want to support differences across terminals, but where common functionality exists we want to provide it through a common interface. The solution is to describe individual pieces of functionality as separate interfaces. For example, we have a @:api(terminus.effect.Writer) interface that allows us to write characters to the Terminal, @:api(terminus.effect.Color) for changing foreground and background colors, and so on. These interfaces, and their implementations, are called *effects* because they actually do things to the terminal.

A backend specific `Terminal` type is the union of the interfaces, or effects, that describe the functionality it supports. For example, here is the JVM `Terminal` type at the time of writing:

```scala
type Terminal =
    effect.AlternateScreenMode,
      effect.ApplicationMode,
      effect.Color,
      effect.Cursor,
      effect.Format,
      effect.Dimensions,
      effect.Erase,
      effect.KeyReader,
      effect.NonBlockingReader,
      effect.Peeker,
      effect.RawMode,
      effect.Reader,
      effect.Writer
```

As the above suggests, the interfaces defining the functionality live in the package `terminus.effect`.

Let's now see an example of an effect type. Here's @:api(terminus.effect.Writer), one of the most basic types:

```scala
/** Interface for writing to a console. */
trait Writer extends Effect {

  /** Write a character to the console. */
  def write(char: Char): Unit

  /** Write a string to the console. */
  def write(string: String): Unit

  /** Flush the current output, causing it to be shown on the console. */
  def flush(): Unit
}
```

As we can see, it just defines some abstract methods that will be implemented in a backend specific way. The effect type can provide a default implementation where there is a reasonable cross-backend way to do so. Here's @:api(terminus.effect.Erase), which sends standard escape codes. A backend specific implementation could override this if there was some advantage to doing so.

```scala
/** Functionality for clearing contents on the terminal. */
trait Erase extends Writer {
  object erase {

    /** Erase the entire screen and move the cursor to the top-left. */
    def screen(): Unit =
      write(AnsiCodes.erase.screen)

    /** Erase from current cursor position to the end of the screen. */
    def down(): Unit =
      write(AnsiCodes.erase.down)

    /** Erase from current cursor position to the start of the screen. */
    def up(): Unit =
      write(AnsiCodes.erase.up)

    /** Erase the current line. */
    def line(): Unit =
      write(AnsiCodes.erase.line)
  }
}
```

There are a few things to note:

1. `Erase` extends `Writer`, and uses the `write` method from `Writer`.
2. The methods are name-spaced by putting them inside an object named `erase`. This is aesthetic choice, leading to method calls like `erase.up()` and `cursor.up()` instead of `eraseUp()` and `cursorUp()`.

There is one final kind of effect that is more involved, which is an effect that only applies to some scope. @:api(terminus.effect.Format) is an example, as is @:api(terminus.effect.Color). These all change the way output is formatted, but only for the `Program` they take as a parameter. For example, when we create a program like 

```scala
Terminal.invert(Terminal.write("Inverted"))
```

we want only the `Program` inside the call to `Terminal.invert` (that `Program` is `Terminal.write("Inverted")`) to be formatted with inverted text. 

Here's part of the implementation of `Format`, which is a bit simpler than `Color`.

```scala
trait Format extends WithStack, WithToggle { self: Writer =>
  object format {
    // ...

    private val invertToggle =
      Toggle(AnsiCodes.format.invert.on, AnsiCodes.format.invert.off)

    def invert[A](f: () => A): A =
      withToggle(invertToggle)(f)
    
    // ...
  }
}
```

The types look a bit odd. The `invert` method doesn't receive a `Program`, with type `F ?=> A`, but a thunk with type `() => A`. The reason for this is that it's a bit tricky to write down the type of `F` at this point. It's whatever terminal effects the program `f` needs, which must also be the same as the type that the particular instance of `Format` is mixed into. We can define this, using a type parameter on `Format`, like so:

```scala
trait Format[F <: Writer] extends WithStack[F], WithToggle[F] { self: F =>
  def invert[A](f: F ?=> A): A =
    withToggle(invertToggle)(f(self))
}
```

A previous version of Terminus did this. However this ends up causing problems in other parts of the code. The issue is that `F` ends up a recursive type, which we define like

```scala
Terminal extends Format[Terminal]
```

We can only define recursive types in concrete types like a `trait` as done above. This in turn means that we cannot write down the types of programs that work across multiple backends, unless we can arrange in advance for all backends to implement some common type. This goes against the idea of different backends implementing only the features they support.

Our solution is to push the `F` type, and the application to the `Program`, into what we call program syntax. What `invert` receives is a thunk that wraps up the application of `Program` to `F`. Let's look at this "syntax" now.


## Program Syntax

Now let's look at how the `Program` types are implemented. We'll start with @:api(terminus.Writer), as it is very simple.

```scala
/** Interface for writing to a console. */
trait Writer {

  /** Write a character to the console. */
  def write(char: Char): effect.Writer ?=> Unit =
    effect ?=> effect.write(char)

  /** Write a string to the console. */
  def write(string: String): effect.Writer ?=> Unit =
    effect ?=> effect.write(string)

  /** Flush the current output, causing it to be shown on the console. */
  def flush(): effect.Writer ?=> Unit =
    effect ?=> effect.flush()
}
```

Firstly, notice this is the `Writer` *program* **not** the `Writer` effect. They are separate concepts. A `Program` is just `Terminal ?=> A`. In the specific case of `Writer` these programs simply require a `Writer` effect and then call the appropriate method on that effect.

These definitions are very simple, and the user could write them themselves. However, it makes life easier if we provide these definitions for them. This is why we call this "program syntax"; we're providing some syntax to make writing programs easier.

`Format` is a bit more complex. Here's the definition, with most of the methods removed.

```scala
trait Format {
  object format {
  
    // ...

    def invert[F, A](
        f: F ?=> A
    ): (F & effect.Format[F]) ?=> A =
      effect ?=> effect.format.invert(() => f(using effect))

    // ...
  }
}
```

`invert` wraps a `Program` with another `Program`. What is the type of the inner program, the method argument `f`? It is whatever effects it requires to run, represented by the type parameter `F`. The result of calling `invert` is another program that requires all the effects of the argument `f` *and* the `Format` effect. In this way programs are constructed to require only the effects they need to run. So long as we apply them to a concrete `Terminal` type that is a super-type of these effects they can be run. At the point of use all these types are known and type inference is easy.

Now the bit that ties into the definition of the `Format` effect above. Notice in the body of `invert` we construct the thunk

```scala
() => f(using effect)
```

This applies `f` to the instance of the effect type `F`, but by wrapping it in a thunk it is delayed until the `invert` effect chooses to run it—which is after the inversion escape code has been sent to the terminal. This gives the effect full control over order-of-evaluation, but still keeps type inference simple. Hence the effect types and the program syntax work together to implement a system that is correct, usable, and has simple types.


## Low-level Code

All the ANSI escape codes used by Terminus are defined in `terminus.effect.AnsiCodes`.
This can be useful if you want to write [escape codes][ansi-escape-codes] directly to the terminal without the abstractions provided by the Terminus DSL.
Here's a simple example.

```scala mdoc
import terminus.effect.AnsiCodes

AnsiCodes.foreground.red
AnsiCodes.erase.line
```

[context-function]: https://docs.scala-lang.org/scala3/reference/contextual/context-functions.html
