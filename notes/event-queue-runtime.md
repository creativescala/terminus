# Event Queue and Cats Effect Runtime (#47)

Agreed design for giving the ui runtime a notion of time: timers, animation,
and off-loop event producers, without disturbing the zero-dependency blocking
path. See issue #47. Enabling step #46 (pure incremental `KeyParser`) is done.

## The unifying idea

Everything that drives the loop other than a key press is really "wake the
loop and run this write on the loop thread". So the queue does not need a
rich event algebra:

```scala
enum Event:
  case Input(key: Eof | Key)   // dispatched to key handlers
  case Effect(run: () => Unit) // a signal write marshaled onto the loop
```

A timer tick is `Event.Effect(() => ticks.update(_ + 1))`; a resize is
`Event.Effect(() => terminalSize.set(dims))`; an application event from an
off-loop producer is an `Event.Effect` writing a signal. The reactive graph
stays effectively single-threaded because the only thing that touches it is
the single consumer draining this queue. This is why `WritableSignal`
creation goes through `React`: the framework can hand off-loop writers a
thread-safe submit that enqueues an `Event.Effect` instead of writing
directly.

The case is named `Effect` (what it *is*) rather than `Wake` (what the
runtime does with it). It is a distinct type from `react.Effect`; as a
nested case it is always written `Event.Effect`, so the names do not collide
in practice. Similarly `runtime.Event` is distinct from `capability.Event`.

## Loop shape

`FullScreen.eventLoop` installs root handlers, constructs the render effect
(drawing the first frame), and returns a step function `Event => Boolean`
(false = quit, tracked by `Runtime.requestQuit`/`quitRequested`). A step
handles one event then drains the effect queue, so one event's writes produce
at most one frame. The end-of-batch "drain, render, diff-flush" boundary is
unchanged — this is also why #45 (shelved) never interacted with this work.

Two runners feed the step function:

- **Blocking runner** (`FullScreen.run`, in `ui`, no new deps): today's
  behavior — `readKey()`, refresh the terminal-size signal, step. No queue,
  no timers.
- **CE runner** (new module): producer fibers feed a
  `cats.effect.std.Queue[IO, Event]`; a single consumer fiber calls step.
  Producers: key-reading fiber, timer fibers, SIGWINCH → resize
  `Event.Effect` on JVM/Native (replacing per-key size polling).

## CE key driver — the timeout pitfall

The driver loops `KeyParser` states exactly like `TerminalKeyReader`, but
`IO.timeout` around `IO.blocking(stdin.read())` does **not** cancel the
underlying read: the abandoned blocked thread eventually swallows the next
keystroke. Implemented design (`terminus.ce`, in `core-ce`):

- `CharSource.pump` runs a background fiber pushing chars into a
  `Queue[IO, Eof | Char]` and yields the queue's take action. The fiber
  reads with core's *timed* `NonBlockingReader.read(pollInterval)` under
  `IO.blocking`, so it is never parked in an uncancelable indefinite read:
  cancelation lands at a poll boundary, bounding shutdown latency at the
  poll interval (default 50ms) without touching input latency (a timed read
  returns as soon as a char is available). The pump stops after `Eof`.
- `KeyReader.readKey(chars, timeout)` drives `KeyParser`, reading with
  `chars.timeoutTo(timeout, Timeout)` while disambiguating — canceling a
  queue take is safe, so the escape-disambiguation timeout is honestly
  `IO.timeout`.

## Modules

Mirror the core/ui split:

- **`core-ce`** (depends on `core` + cats-effect): char source, CE `readKey`
  driver; later the #27 territory (terminal modes as `Resource`).
- **`ui-ce`** (depends on `ui` + `core-ce`): the event queue, CE runner for
  `FullScreen`, timer capability.

## Time as a capability

Timers do not go on `React`: apps on the blocking runtime cannot have them,
and capabilities are how Terminus says "can't" at compile time. A separate
capability provided only by the CE runtime:

```scala
trait Timer:
  def every(interval: FiniteDuration): Signal[Long]     // spinners, blink
  def after(delay: FiniteDuration)(f: () => Unit): Unit // pressed-flash
```

Implemented as fibers doing `IO.sleep *> submit(...)`. Timer fibers are the
first real external resource the framework creates on a component's behalf —
the first genuine customer for #42's `onCleanup`/disposal story. #42 need
not land first (screen-lifetime fibers canceled when the runner exits is
fine initially), but its design should assume cancelable external resources,
not just signal unsubscription.

## Implementation order

1. ~~Extract `step` from `FullScreen.run`; blocking runner keeps current
   behavior (pure refactor).~~ Done.
2. ~~`core-ce`: char-queue pump + CE `readKey` driver, with timeout
   tests.~~ Done.
3. ~~`ui-ce`: event queue + consumer fiber + CE `FullScreen` runner, no
   timers yet — parity milestone.~~ Done (JVM): `Events.keys`/`Events.consume`
   (shared) + `Runner.run` (jvm). One wart, by design: core's terminal modes
   are synchronous brackets, so `Runner.run` enters them on a blocking thread
   and runs the concurrent session inside via `Dispatcher.unsafeRunSync` —
   consequently canceling the runner's `IO` does not stop the session; quit
   comes from within (Ctrl+Q / Eof). #27 (modes as `Resource`) removes this
   inversion. The runner lives in a `jvm-native` source directory: Scala
   Native has supported threads since 0.5.10 and Cats Effect works with them,
   so JVM and Native share it; JS needs an event-driven driver instead of the
   char pump. Parity demos: `sbt 'uiCeJVM/runMain terminus.ui.ce.demo'` and
   `sbt 'uiCeNative/runMain terminus.ui.ce.demo'`.
4. `Timer` capability + spinner/blink demo — the payoff milestone.
5. SIGWINCH → resize `Event.Effect` (JVM/Native), replacing per-key size
   polling in the CE runner.
