# Event Queue and Runtime

How the ui runtime gets a notion of time (timers, animation, off-loop event
producers) without disturbing the zero-dependency blocking path, and how
interactions with the outside world (terminal I/O, scheduling) are mediated.
Originally designed for #47; substantially reworked by a later "introduce a
Runner" refactor that replaced the framework-internal `EffectQueue` with a
`Runner` abstraction and a `DefaultRuntime` that owns focus/dispatch state
directly. This note describes the current design; see git history for the
#47-era `EffectQueue`/two-runner-methods shape if you need the prior art.

## The unifying idea

Everything that drives the loop other than a key press is really "wake the
loop and run this write on the loop thread". So the queue does not need a
rich event algebra:

```scala
private[ui] enum Event:
  case Input(key: Eof | Key)              // dispatched to key handlers
  case Effect(run: Runnable)              // a signal write marshaled onto the loop
  case Resize(dimensions: TerminalDimensions)
```

A timer tick is an `Event.Effect` wrapping a `Runnable` that updates a
signal; a resize is `Event.Resize`; an application event from an off-loop
producer is an `Event.Effect` writing a signal. The reactive graph stays
effectively single-threaded because the only thing that touches it is
`FullScreen.run`'s loop, the single consumer draining this queue. This is
why `WritableSignal` creation goes through `React`: the framework can hand
off-loop writers a thread-safe submit that enqueues an `Event.Effect`
instead of writing directly.

## The Runner

`Runner` (`terminus.ui.runtime`) is the interface between the framework and
the outside world:

```scala
trait Runner:
  def schedule(io: IO[Runnable]): Unit
  def schedule(stream: Stream[IO, Runnable]): Unit
  def run(terminal: effect.Dimensions & effect.NonBlockingReader): BlockingQueue[Event]
```

`schedule` accepts work that will eventually produce a `Runnable` to run on
the loop thread — this is how a React effect becoming stale, or a timer
tick, reaches the reactive graph without a data race. `run` starts the
Runner's producers (key reads, resize polling, whatever else) and returns
the `BlockingQueue[Event]` that `FullScreen.run`'s loop takes from.

There is currently one implementation, `CatsEffectRunner`, which works in
two stages: setup time, when effects can be `schedule`d but nothing is
consuming them yet, and run time, once `run` has started the consumer
pipeline. An `inQueue: Queue[IO, Stream[IO, Runnable]]` bridges the two —
`schedule` offers onto it regardless of whether `run` has been called yet
(the queue just buffers), so there is no special-casing for "scheduled
before the session exists" any more. This is what let #47's two-phase
`DefaultTimer` (record tasks, replay them on `connect`) collapse into a
plain forwarding call: the buffering that used to be `DefaultTimer`'s job is
now the Runner's, for free, for every scheduler not just timers.

Inside `run`, a `Supervisor` owns the fibers so anything still running is
canceled when the session ends: one fiber per `schedule`d `Stream`/`IO`
(each pushes its `Runnable`s onto an internal `outQueue: BlockingQueue[Event]`
wrapped as `Event.Effect`), one resize-polling fiber (polls
`getDimensions` every 100ms, offers `Event.Resize` only on change — see
"resize" below), and the key-reading loop, which is the innermost fiber
since it's the one that signals stop (`Eof`).

`DefaultRuntime` (implements `Runtime`, which extends the app-facing
`Schedule` capability) holds this Runner and forwards to it:
`schedule(effect: Effect)` becomes `runner.schedule(IO.pure(effect))`, and
`DefaultTimer`'s `every`/`after` become `runner.schedule(stream)` /
`runner.schedule(io)` directly (see below). `DefaultRuntime` otherwise owns
what the old `Runtime` class used to: focus order, enabled predicates, root
handlers, `dispatch`.

## Loop shape

`FullScreen.run` (`terminus.ui.FullScreen`, not a separate `ui.ce` module)
installs root handlers (tab/shift-tab focus, Ctrl+Q quit), builds the first
frame, calls `runner.run(terminal)` to get the event queue, then loops:

```scala
def step(): Unit =
  queue.take() match
    case Event.Input(Eof)      => runtime.quit()
    case Event.Input(key: Key) => runtime.dispatch(key)
    case Event.Effect(run)     => run.run()
    case Event.Resize(dim)     => currentSize = dim

def loop(): Unit =
  if runtime.quitRequested then ()
  else step(); render(); loop()
```

One event, one render — a handler that writes several signals in one key
dispatch still produces a single frame, because a signal write only marks
its effect stale (schedules it); the effect doesn't actually run until its
`Event.Effect` is taken off the queue and `run()`'d, which happens as part
of the very step that dispatched the key (writes route straight to
`runtime.schedule` → `Runner.schedule(IO.pure(...))`, and on the blocking
path that resolves synchronously enough to land before `render()`). There
is no separate blocking runner any more — `CatsEffectRunner` is the only
implementation, so `ui` unconditionally depends on `core-ce` and
cats-effect (see "Modules" below; this is what "folded `ui-ce` into `ui`"
in the CLAUDE.md history refers to, taken one step further).

## CE key driver — the timeout pitfall

The driver loops `KeyParser` states exactly like `TerminalKeyReader`, but
`IO.timeout` around `IO.blocking(stdin.read())` does **not** cancel the
underlying read: the abandoned blocked thread eventually swallows the next
keystroke. Implemented in `core-ce`'s `terminus.ce`:

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

`CatsEffectRunner.run` drives this directly (`CharSource.fromReader(terminal).use { ... }`
looping `KeyReader.readKey`) rather than through a separately-testable
`Events.keys` helper — an earlier version of this module (`terminus.ui.ce.Events`,
with `keys`/`resizes`/`consume` functions and its own `EventsSuite`) was
retired as dead code once the Runner refactor inlined the equivalent logic;
there is currently no unit coverage for the key-reading/resize-polling
loops themselves, only for the pieces they're built from
(`core-ce`'s `CharSourceSuite`/`KeyReaderSuite`).

## Modules

- **`core-ce`** (depends on `core` + cats-effect): char source, CE
  `readKey` driver; later the #27 territory (terminal modes as `Resource`).
- **`ui`** (depends on `core` + `core-ce`): everything, including the
  runner. There is no more separate `ui-ce`/`ui.ce` module for the runtime
  — folded into `ui` (2026-07) once it became clear no real application
  would use `ui` without animation/timers. The `terminus.ui.ce` *package*
  still exists, but only for the platform-specific `demo` entry points
  (`ui/jvm/.../ui/ce/Demo.scala`, `ui/native/.../ui/ce/Demo.scala`, run via
  `sbt 'uiJVM/runMain terminus.ui.ce.demo'`); the runtime/timer code itself
  lives in `terminus.ui.runtime` and `terminus.ui.timer`.
- `CatsEffectRunner` and `Runner` currently live directly under
  `ui/shared/src/main/scala/terminus/ui/runtime/`, not the JVM/Native-only
  `ui/jvm-native/src/main/scala/` split that the #47-era `ui.ce.Runner`
  used. `CatsEffectRunner` uses `java.util.concurrent.BlockingQueue`, which
  Scala.js's javalib compiles against but cannot meaningfully run
  (single-threaded), so `uiJS` currently builds a `CatsEffectRunner` that
  is not expected to work at runtime — JS still needs its own
  event-driven Runner (no char-pump thread) before `uiJS`'s interactive
  path is real. This is a known gap, not yet tracked back to a specific
  issue.

## Time as a capability

Timers do not go on `React`: apps built without a Runner that has a notion
of time cannot have them, and capabilities are how Terminus says "can't" at
compile time. `Timer` (`terminus.ui.capability.Timer`):

```scala
trait Timer:
  def every(interval: FiniteDuration): Signal[Long]     // spinners, blink
  def after(delay: FiniteDuration)(f: () => Unit): Unit // pressed-flash
```

`DefaultTimer` (`terminus.ui.timer.DefaultTimer`) is now a thin forwarder,
not the two-phase task-recording object #47 originally built:

```scala
trait DefaultTimer(runtime: Runtime) extends Timer:
  def every(interval: FiniteDuration): Signal[Long] =
    val ticks = WritableSignal(0L)
    runtime.schedule(
      Stream.awakeDelay[IO](interval).as((() => ticks.update(_ + 1)): Runnable)
    )
    ticks

  def after(delay: FiniteDuration)(f: () => Unit): Unit =
    runtime.schedule(IO.sleep(delay).as(toRunnable(f)))
```

The two-phase "record before the session exists, replay on connect" dance
is gone: `runtime.schedule` reaches `Runner.schedule`, whose `inQueue`
already buffers offers made before `run` starts consuming, so calling
`every`/`after` from setup scope (before the runner is live) just works —
no separate `Task`/`connect` machinery needed. `every`'s stream is
`awakeDelay`, so it repeats on its own; `after`'s `IO.sleep` fires once.
Both eventually surface as an `Event.Effect` on the loop, so timer writes
reach the reactive graph like every other write. `Row`/`Column` (and every
layout component) now thread `Timer` through alongside `React`/`Layout`/
`Event`, since `AppContext` composes all of them.

## Testing without a real Runner

Application/framework tests that only exercise focus and dispatch (not
scheduling) use `Runtime.empty` — a `DefaultRuntime` backed by
`Runner.noop`, which discards anything scheduled through it. Tests that
need to observe scheduling deterministically (`EffectSuite`,
`DefaultTimerSuite`) use `TestRunner`
(`ui/jvm/src/test/scala/terminus/ui/runtime/TestRunner.scala`), which
captures `IO[Runnable]`s and `Stream[IO, Runnable]`s instead of running
them on real fibers, so a test can `drain()` an `IO` queue deterministically
or pull a fixed number of elements from a `Stream` (e.g. `.take(2)`) to
simulate ticks. Because this needs real Cats Effect `IO` execution
(`unsafeRunSync`, unavailable on Scala.js), these tests and `TestRunner`
live under `ui/jvm` rather than `ui/shared` — the same boundary the #47-era
`EventsSuite` used, now applied more broadly since scheduling itself is
IO-shaped.

## Status

The original #47 plan (steps 1-5: extract `step`, `core-ce` char pump +
driver, CE runner parity milestone, `Timer` capability, resize as an
`Event.Effect`) shipped and closed. The later Runner refactor kept the
external behavior (same `Event` shape, same one-event-one-render loop, same
resize-polling and timer designs) while replacing the internal plumbing:
`EffectQueue` → `Runner`/`DefaultRuntime`, `ui.ce`'s two-phase `DefaultTimer`
→ a thin forwarder, `ui.ce.FullScreen`/`ui.ce.Runner` → `ui.FullScreen` +
`ui.runtime.CatsEffectRunner`. Open follow-on threads: #27 (terminal modes
as `Resource`, removing the runner's bracket inversion — `CatsEffectRunner.run`
still enters raw/alternate-screen modes on a blocking thread and runs the
concurrent session via `Dispatcher.unsafeRunAndForget`, so canceling the
runner's `IO` does not stop the session; quit comes from within, Ctrl+Q or
`Eof`); #42's cleanup/disposal design should account for timer fibers;
a JS-native Runner (event-driven, no char-pump thread) is needed before
`uiJS` is actually interactive; and reviving `Events`-style extracted
producer functions (with unit tests) for `CatsEffectRunner.run`'s
key/resize loops would restore the coverage the old `EventsSuite` gave.
