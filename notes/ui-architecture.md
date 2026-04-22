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

Rather than FRP's higher-order style (`signal.map(v => ...)`) the reactive context is a capability. Reading a signal inside a component body — `signal.get` — implicitly registers the component as a subscriber via the `ComponentContext` in scope. This is the "events as capabilities" solution: first-order code, no explicit subscription plumbing.

## Key types

### Rendering

- **`Cell(codePoint: Int, style: Style)`** — a single terminal cell. `codePoint = 0` is the continuation sentinel for the right half of wide characters.
- **`Buffer(width, height)`** — row-major flat array of cells. Out-of-bounds writes are silently ignored (component isolation). Key methods: `put`, `fill`, `putString`, `render`, `renderDiff`.
- **`Rect(x, y, width, height)`** — 0-based position + size. 1-based conversion happens only inside `Buffer.render`.
- **`Style`** — cell-level attributes: fg/bg color, bold, italic, underline, blink, invert, strikethrough.

### Capabilities

- **`RenderContext`** — layout effect. Components add children to it; the call stack implicitly encodes the layout tree. `RootContext` flushes to terminal; `ChildContext` renders into a buffer region.
- **`EventContext`** — the reactive runtime. Owns signals (their lifetime is tied to the context), registers key handlers, and schedules re-renders when signals change.
- **`ComponentContext`** — the reactive scope for a single component during render. `Signal.get` uses it to register the component as a subscriber. When the signal changes, `invalidate()` is called on all subscribers, triggering a re-render.

### Signals

```scala
trait ReadSignal[A]:
  def get: A

trait Signal[A] extends ReadSignal[A]:
  def set(a: A): Unit
  def update(f: A => A): Unit = set(f(get))
```

Signals are created and owned by an `EventContext`. Their lifetime matches the context's lifetime — useful for screen-level resource management.

Currently `get` has no tracking: any signal change triggers a full-frame re-render regardless of which components read that signal. The intended future API is `get(using ComponentContext)` (tracked) and `peek: A` (untracked, for use in event handlers), which would enable subtree re-rendering.

### `AppContext` and content type aliases

`AppContext` is a trait combining `ComponentContext`, `EventContext`, and `RenderContext`. It is the single context type passed through the component tree. Layout components (Row, Column) use `AppContext.child(parent, rc)` to produce a child context that delegates signal creation and event registration to the parent but uses a new `RenderContext` for sub-layout.

To avoid verbose context function types at every call site:

```scala
// Content of a leaf component (e.g. Text): can read signals, no sub-layout
type LeafContent[A] = ComponentContext ?=> A

// Body of a layout component (e.g. Row, Column): full reactive + layout context
type AppContent[A] = AppContext ?=> A
```

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

1. **Setup** (runs once): the app function is called with `AppContext` in scope. Signals are created, key handlers registered, and the component tree built.
2. **Render** (runs on signal change): the component tree's render methods are called. Leaf components evaluate their `LeafContent` closures with a fresh `ComponentContext`, which re-registers signal dependencies and returns the current value.
3. **Event dispatch**: the event loop reads a key, dispatches to registered handlers. Handlers inside a `FocusScope` are gated on focus; global handlers always fire.
4. **Invalidation and re-render**: `Signal.set` calls `scheduleRerender()`. Currently this triggers a full-frame re-render; later, subtree re-rendering will be possible once bounds are captured in the `ComponentContext`.

### Setup scope vs render scope

This distinction is the most important thing to understand about the programming model, and currently the most error-prone.

**Setup scope** is the `AppContent` body. Code here runs *once* when the screen is first built. It is the right place for: creating signals, registering key handlers, and building the component tree. Reactive values read here (e.g. `signal.get`, `ctx.isFocused`) capture a single value and do not update.

**Render scope** is a leaf component's content lambda — the `=> String` passed to `Text`, for example. This code runs on *every render pass*. Reactive values read here return their current value each frame. This is where signal reads and `isFocused` checks belong.

The current API does not enforce this distinction at the type level — both scopes receive an `AppContext` / `ComponentContext`, so nothing stops you from reading a signal in setup scope and getting a stale value. This is expected to resolve naturally when dependency tracking is added: `signal.get` will require a `ComponentContext` in scope, which is only available inside content lambdas, making the setup/render boundary compile-time enforced.

## Effect layer

`terminus.ui.Terminal = effect.AlternateScreenMode & effect.Erase & effect.Color & effect.Cursor & effect.Format & effect.Writer`

Components do not receive `Terminal`. Only `Buffer.render` / `Buffer.renderDiff` touch the terminal.

The interactive event loop additionally requires `effect.KeyReader & effect.RawMode`.

## Wide character support

`CharWidth.of(codePoint)` returns 0, 1, or 2 per Unicode TR#11. Wide characters occupy two cells: left cell holds the character, right cell holds `Cell.continuation`. `putString` handles this automatically.

## Double buffering

`Buffer.renderDiff(previous)` compares two same-sized buffers and emits only changed cells, with cursor-position tracking to suppress redundant `cursor.to` calls for adjacent changes.

## Deferred work

- **Focus ergonomics**: basic focus is implemented via `FocusScope` and `isFocused`. The remaining rough edge is that `isFocused` must be read inside a leaf content lambda (render scope) — reading it in the `FocusScope` body (setup scope) captures a stale value. This will resolve when dependency tracking enforces the setup/render boundary at the type level.

- **Dynamic layout**: component sizes are currently fixed at construction time (e.g. `Text(width, height)`). The layout pass runs once during setup, so components cannot resize in response to signal changes. Dynamic layout requires either re-running the setup phase on resize, or a proper two-pass layout system (measure → arrange) that runs each frame.

- **More components**: at minimum, a text input / field component is needed for interactive use.

- **Subtree re-rendering**: `ComponentContext` currently triggers a full-frame re-render. To support subtree re-rendering, `ComponentContext` needs to capture the component's bounds and buffer reference so it can re-render only the affected region. Depends on signal tracking (`get(using ComponentContext)`) being implemented first.

- **Derived signals**: `ReadSignal.map` is deferred. Manual handler functions updating signals are sufficient for now.

- **Resize handling**: terminal resize events (`SIGWINCH`) are not yet handled. Dynamic layout is a prerequisite for this to be useful.

- **`effect.Dimensions` on Scala Native**: requires Posix `ioctl` — may be better to contribute upstream than implement in Terminus.
