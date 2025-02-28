# Quick Start

Follow these steps to create a Terminus project.


## Pre-requisites

You must have [sbt] installed. [Follow the instructions][download] to install sbt (and Scala) if you don't already have it.


## Basic Project

Run

```bash
sbt new scala/scala3.g8
```

to create a new project. It doesn't matter what name you give this project. Something simple, like `terminal`, will do.

Open `build.sbt` in the project you just created. Above the line that starts with `libraryDependencies` add

```scala
libraryDependencies += "org.creativescala" %%% "terminus-core" % "@VERSION@"
```

Note the trailing comma on this line. sbt will be upset if you don't add this.

Now open `src/main/scala/Main.scala` and replace the code with

```scala
import terminus.*

@main def hello(): Unit =
  Terminal.run {
    Terminal.format.bold {
      Terminal.foreground.green {
        Terminal.write("This is Terminus!")
        Terminal.flush()
      }
    }
  }
```

Now run the command `sbt run` and you should see output like that below on the terminal.

@:doodle("color-foreground-green", "ColorForegroundGreen.go")

That's all you need to get started. Read the rest of the documentation to learn more about Terminus' capabilities.

[sbt]: https://www.scala-sbt.org/
[download]: https://www.scala-lang.org/download/
