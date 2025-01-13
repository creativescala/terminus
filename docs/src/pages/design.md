# Design

Terminus is split into two parts: `terminus.effect` which defines all the effects that can be used in terminal, and `terminus`, which defines the user-facing functions for interacting with effects.


## Low-level Code

All the ANSI escape codes used by Terminus are defined in `terminus.effect.AnsiCodes`.
This can be useful if you want to write [escape codes][ansi-escape-codes] directly to the terminal without the abstractions provided by the Terminus DSL.
Here's a simple example.

```scala mdoc
import terminus.effect.AnsiCodes

AnsiCodes.foreground.red
AnsiCodes.erase.line
```
