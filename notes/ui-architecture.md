# UI Module Architecture

## Overview

The `ui/` module builds a terminal UI toolkit on top of the core effect layer. It uses a **capability-passing** style throughout: effects and reactive context are passed as Scala 3 context parameters (`using` / `?=>`), making dependencies explicit and enforced at compile time.

Rendering uses a **cell buffer** model: components write into an in-memory 2D grid of cells, then a single controlled flush to the terminal at the end of each frame.

## The two-structure problem

Every UI framework must express two distinct but intertwined structures:

1. **Layout** — tree-shaped. Components are nested inside containers.
2. **Events / control flow** — graph-shaped, often circular. A button may need to reference an input field (to clear it on click), and the input field may need to reference the button (to enable or disable it). This mutual dependency cannot be expressed as a tree.

Different frameworks resolve this tension differently:

- **Smalltalk**: layout as data, events as side effects. Recursion handled by delayed state mutation.
- **FRP**: layout as data, events as first-class streams. Recursion handled via fixed-point combinators, but requires higher-order functional style.
- **Immediate mode / Jetpack Compose**: layout as an *effect* (mutating implicit global state). The call stack implicitly represents the layout tree, eliminating one explicit structure. Events are values; the reactive runtime re-runs affected computations.

Our approach follows the Jetpack Compose model but makes the implicit capabilities explicit via context parameters. The "immediate mode re-runs everything each frame" behaviour is an artifact of game-engine contexts and languages with limited graph-structure support — not a fundamental property of the model.

### Solving the circular event dependency

The circular dependency between components is broken by **signals**: shared reactive values that components reference indirectly. Neither component holds a reference to the other; both hold a reference to the same `Signal`. This is the "events as values" solution.

Rather than FRP's higher-order style (`signal.map(v => ...)`) the reactive context is a capability. Reading a signal inside a component body — `signal.get` — implicitly registers the component as a subscriber via the `Observe` capability in scope. This is the "events as capabilities" solution: first-order code, no explicit subscription plumbing.

## Key types

### Rendering

- **`Cell(codePoint: Int, style: CellStyle)`** — a single terminal cell. `codePoint = 0` is the continuation sentinel for the right half of wide characters.
- **`Buffer(width, height)`** — row-major flat array of cells. Out-of-bounds writes are silently ignored (component isolation). Key methods: `put`, `fill`, `putString`, `render`, `renderDiff`.
- **`Rect(x, y, width, height)`** — 0-based position + size. 1-based conversion happens only inside `Buffer.render`.
- **`Style`** — cell-level attributes: fg/bg color, bold, italic, underline, blink, invert, strikethrough.

### Capabilities

There is no single combined context type any more (no `AppContext`, no
`RenderContext`/`EventContext`/`LayoutContext`). Instead there are several
small capability traits in `terminus.ui.capability`, and a component's body
takes exactly the intersection it needs as a context-function parameter,
e.g. `FullScreen`'s body is `(Layout & React & Timer) ?=> Unit`, `Column`'s
is `(Event & Layout & React & Timer) ?=> Unit`, `Text`'s content is
`(Event & React) ?=> Signal[text.Text]`:

- **`Layout`** — `def addComponent(component: Runtime => Component): Unit`. Components add children to it; the call stack implicitly encodes the layout tree. Implemented by `DefaultLayout`.
- **`Event`** — registers key handlers (`onKey`, `onAnyKey`) and exposes a component's `focus`/`availability` as signals; `enabledWhen` drives availability reactively. Implemented by `DefaultEvent`.
- **`React`** — the capability to create reactive values (`signal`, `computed`, `effect`); see below. Implemented by `DefaultReact`.
- **`Observe`** — the capability to read signals reactively (`get`); see below.
- **`Timer`** — `every`/`after`, for timer-driven state; only available where the `Runner` has a notion of time. Implemented by `DefaultTimer`. See `notes/event-queue-runtime.md`.
- **`Schedule`** — `def schedule(effect: Effect): Unit`, the framework-internal capability an `Effect` uses to get itself queued when it goes stale. Implemented by `Runtime`/`DefaultRuntime`, not exposed to application code.

`FullScreen.withLayout` builds one object implementing `DefaultEvent`,
`DefaultLayout`, `DefaultReact`, and `DefaultTimer` together, all backed by
the same `DefaultRuntime`, and passes it as the `given` for the body's
context-function parameter. There's no per-child context object distinct
from this: `Layout.addComponent` records `Runtime => Component` thunks
directly (see `Component` trait below) rather than a tree of child
contexts.

### Signals

```scala
sealed trait Signal[A]:
  def peek: A
  def get(using ctx: Observe): A
  def map[B](f: A => B)(using r: React): Signal[B]

final class WritableSignal[A] extends Signal[A]:
  def set(a: A): Unit
  def update(f: A => A): Unit
```

`Signal[A]` is the read-only interface (`get`/`peek`/`map`), `WritableSignal[A]` adds `set`/`update`, and `Computed`/`Constant` are private implementations behind `Signal`. Signals are created through the `React` capability (or, for values that never change, `Signal.constant`) — see "Rendering is an effect" below for why creation is capability-gated rather than a bare constructor. `get(using Observe)` is tracked: it subscribes the enclosing tracked computation. `peek` is untracked, for event handlers and other code outside a render pass. Granularity is still whole-frame — any tracked dependency change redraws everything — but the tracking is real: a frame renders only when something it actually read has changed.

### Component trait

```scala
trait Component:
  def size: Size
  def render(bounds: Rect, buf: Buffer): Unit
```

## Layout

Two-phase: layout pass (size accumulation via `add`) then render pass (writing to buffer via `render`).

- **`Row`** — horizontal layout, accumulates x offsets.
- **`Column`** — vertical layout, accumulates y offsets.
- **`FullScreen`** — root context, creates buffer sized to content (not terminal dimensions — avoids `effect.Dimensions` which is not implemented on Scala Native).

## Lifecycle

1. **Setup** (runs once): the app function is called with the composed capabilities (`Event & Layout & React & Timer`, or the subset a given body declares) in scope. Signals are created, key handlers registered, and the component tree built.
2. **Render** (runs on signal change): the component tree's render methods are called. Leaf components evaluate their content thunks with a fresh `Observe`, which re-registers signal dependencies and returns the current value.
3. **Event dispatch**: `Runtime.dispatch` reads a key, checks root handlers first, then delivers to the focused component if it's enabled — see `Availability`/`Focus` in `capability.Event`.
4. **Invalidation and re-render**: writing a signal marks its subscribers stale, which schedules them (`Schedule.schedule`, routed through the `Runner`). The event loop runs newly-scheduled effects (as `Event.Effect`s taken off the queue) as part of the same step that triggered them, so a handler that writes several signals produces one frame. If nothing a frame read has changed, no frame is drawn.

### Setup scope vs render scope

This distinction is the most important thing to understand about the programming model, and currently the most error-prone.

**Setup scope** is a component body — the thunk passed to `FullScreen`, `Row`, `Column`, etc. Code here runs *once* when the screen (or that component) is first built. It is the right place for: creating signals, registering key handlers, and building the component tree. Reactive values read here (e.g. `signal.peek`) capture a single value and do not update.

**Render scope** is a leaf component's content thunk — the `Observe ?=> Signal[text.Text]` passed to `Text`, for example, or a component's `measure`/`render`. This code runs on *every render pass*. Reactive values read here (`signal.get`) return their current value each frame. This is where reactive reads belong, including a component's `focus`/`availability` signals.

The distinction is enforced at the type level by two capabilities with deliberately disjoint scopes:

- **`Observe`** (render scope) — the capability to *read* signals reactively. `signal.get` requires it, and it is only available inside tracked computations: measure/intrinsics/render, and the thunks of computeds and effects. In setup or handler code, `get` does not compile; use `peek`.
- **`React`** (setup scope) — the capability to *create* reactive values: `signal(initial)`, `computed { ... }`, `effect { ... }`. It is only available in setup code (the body passed to `FullScreen` or to a component), so reactive values cannot be created inside a render pass, where they would be recreated every frame and never disposed. All creation goes through the capability (including `map`, which needs one) so the implementation can do framework-side wiring: routing effects to the `Runner` and, in future, registering created values for disposal with their owning component. The capability is implemented by the same per-component objects (`DefaultEvent`/`DefaultLayout`/`DefaultReact`/`DefaultTimer`, all backed by one `DefaultRuntime`) that implement `Event` and `Layout` — which is how, when ownership lands, a component will own what its body created.

## Rendering is an effect

The whole frame is a single `Effect` (react package): an eagerly-run thunk that
re-tracks its reactive dependencies on every run, like a `Computed` whose
staleness schedules a re-run instead of waiting to be pulled.
`FullScreen.run` (`terminus.ui.FullScreen`) constructs the effect (drawing
the first frame), starts the `Runner` (`runner.run(terminal)`, which returns
the `BlockingQueue[Event]` producers feed), and loops: take one event, handle
it (dispatch a key, run an `Event.Effect`'s `Runnable`, or apply a resize),
render, repeat — one event, one render. There is a single `Runner`
implementation, `CatsEffectRunner`: producers (keys, timers, resizes) feed
the queue and the loop is the single consumer. See
`notes/event-queue-runtime.md` for the Runner/`DefaultRuntime` split and why
the earlier two-runner (blocking vs. Cats Effect) design collapsed into one.

Effects are leaves of the reactive graph: they produce no value, nothing can
depend on them, and they have no combinators — composition happens on the
value side (`computed`, `map`), with an effect at the bottom. Applications
create them through `React.effect`, which returns `Unit`; only the framework
holds `Effect` references. Scheduling one (`Schedule.schedule`, implemented
by `DefaultRuntime` by forwarding to the `Runner`) is framework-internal:
applications can neither schedule nor drain directly, so a handler cannot
force a mid-batch render.

Consequences of the design:

- **There is no `Component.react`.** Measure, the intrinsics, and render take
  `(using Observe)` and read with `get`; the read *is* the subscription. A
  component cannot render from state it isn't subscribed to.
- **Batching.** `setStale` only schedules (via `Runner.schedule`); effects run
  when their `Event.Effect` comes off the queue, once per batch, so handlers
  that write several signals cannot cause glitches or double renders.
- **Terminal size is a reactive input**: a signal the render effect reads, so
  a resize is just another dependency change. The Runner has a resize
  producer that polls for changes and delivers them as `Event.Resize`.
- **Effects must not write signals they read.** The TextInput cursor is the
  example: instead of normalizing the cursor signal each frame, out-of-range
  positions (after an external shrink of the value) are clamped at every read.

Timers/animation landed via the event queue and Cats Effect runner (#47, see
`notes/event-queue-runtime.md`): the loop blocks on a queue rather than
`readKey`, and the `Timer` capability wakes it without a key press — the
payoff of routing `WritableSignal` creation through `React` so off-loop
writers reach the loop. `Timer` is provided by `DefaultTimer`
(`terminus.ui.timer`), composed into the capability intersection alongside
`React`/`Layout`/`Event` — every layout component (`Row`, `Column`, ...)
carries it now, not just `FullScreen`'s body. Per-component effects
(needs-layout/needs-paint) and equality cut-off in `Computed` were closed as
not-planned (#45, #44): whole-frame rendering + diff-flush is the design,
and the incrementalism only saves CPU that terminal-sized workloads don't
spend; revisit as an exploration if ever. Ownership/disposal is still
deferred — see "Deferred work" below.

## Effect layer

`terminus.ui.Terminal = effect.AlternateScreenMode & effect.Erase & effect.Color & effect.Cursor & effect.Format & effect.Writer`

Components do not receive `Terminal`. Only `Buffer.render` / `Buffer.renderDiff` touch the terminal.

The interactive event loop additionally requires `effect.KeyReader & effect.RawMode`.

## Wide character support

`CharWidth.of(codePoint)` returns 0, 1, or 2 per Unicode TR#11. Wide characters occupy two cells: left cell holds the character, right cell holds `Cell.continuation`. `putString` handles this automatically.

## Double buffering

`Buffer.renderDiff(previous)` compares two same-sized buffers and emits only changed cells, with cursor-position tracking to suppress redundant `cursor.to` calls for adjacent changes.

## Deferred work

Resolved since this list was last written: focus/availability (`Event.focus`,
`Availability`, `enabledWhen` — reactive, keyed off the render-scope
`Observe` capability, so there's no stale-value trap left), more components
(`Button`, `Select`, `TextInput` all exist), derived signals (`Signal.map`),
and resize handling (`Event.Resize`, delivered by `CatsEffectRunner`'s
polling producer — see `notes/event-queue-runtime.md`). Still open:

- **Dynamic layout**: component sizes are currently fixed at construction time (e.g. `Text(width, height)`). The layout pass runs once during setup, so components cannot resize in response to signal changes, beyond what `TextInput`'s internal cursor-clamping trick achieves for its own value. Dynamic layout requires either re-running the setup phase on resize, or a proper two-pass layout system (measure → arrange) that runs each frame.

- **Subtree re-rendering**: rendering is currently whole-frame — any tracked dependency change re-renders the whole component tree (then diffed against the previous buffer before anything hits the terminal). Subtree re-rendering would need a render-scope object that captures a component's bounds and buffer region, so a signal change could trigger just that component's render. Closed as not-planned for now (#45, #44): the terminal-sized workloads this targets don't spend enough CPU on a full re-render for the incrementalism to pay for its complexity. Revisit as an exploration if that changes.

- **`effect.Dimensions` on Scala Native**: requires Posix `ioctl` — may be better to contribute upstream than implement in Terminus.

- **Ownership/disposal**: no `dispose()` on effects/computeds yet, no `onCleanup` hook for applications. Becomes necessary once components can be removed from the tree; #42's design should account for timer fibers (#51) as the first per-app external resource. See "Rendering is an effect" above and `notes/event-queue-runtime.md`.

- **JS Runner**: `CatsEffectRunner` (the only `Runner` implementation) is built against `java.util.concurrent.BlockingQueue`, which compiles for Scala.js but isn't meaningfully usable there (single-threaded runtime). `uiJS`'s interactive path needs its own event-driven Runner. See `notes/event-queue-runtime.md`.
