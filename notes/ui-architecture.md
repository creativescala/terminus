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
  def get(using ComponentContext): A  // tracked: registers a dependency
  def peek: A                         // untracked: for use in event handlers

trait Signal[A] extends ReadSignal[A]:
  def set(a: A): Unit
  def update(f: A => A): Unit = set(f(peek))
```

Signals are created and owned by an `EventContext`. Their lifetime matches the context's lifetime — useful for screen-level resource management.

### Component content type aliases

To avoid verbose context function types at every call site:

```scala
// Content of a leaf component (e.g. Text): can read signals, no sub-layout
type LeafContent[A] = ComponentContext ?=> A

// Body of a layout component (e.g. Row, Column): can read signals,
// register handlers, and create sub-components
type AppContent[A] = ComponentContext & EventContext & RenderContext ?=> A
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

1. **Setup** (runs once): the app function is called with `EventContext` and `RenderContext` in scope. Signals are created, key handlers registered, and the component tree built.
2. **Render** (runs on signal change): the component tree's render methods are called. Leaf components evaluate their `LeafContent` closures with a fresh `ComponentContext`, which re-registers signal dependencies and returns the current value.
3. **Event dispatch**: the event loop reads a key, dispatches to registered handlers (global handlers first, then focused component — focus model TBD). Handlers update signals via `set` or `update`.
4. **Invalidation and re-render**: `Signal.set` calls `invalidate()` on all subscribed `ComponentContext`s. Initially this triggers a full-frame re-render; later, subtree re-rendering will be possible once bounds are captured in the `ComponentContext`.

## Effect layer

`terminus.ui.Terminal = effect.AlternateScreenMode & effect.Erase & effect.Color & effect.Cursor & effect.Format & effect.Writer`

Components do not receive `Terminal`. Only `Buffer.render` / `Buffer.renderDiff` touch the terminal.

The interactive event loop additionally requires `effect.KeyReader & effect.RawMode`.

## Wide character support

`CharWidth.of(codePoint)` returns 0, 1, or 2 per Unicode TR#11. Wide characters occupy two cells: left cell holds the character, right cell holds `Cell.continuation`. `putString` handles this automatically.

## Double buffering

`Buffer.renderDiff(previous)` compares two same-sized buffers and emits only changed cells, with cursor-position tracking to suppress redundant `cursor.to` calls for adjacent changes.

## Deferred work

- **Subtree re-rendering**: `ComponentContext` currently triggers a full-frame re-render. To support subtree re-rendering, `ComponentContext` needs to capture the component's bounds and buffer reference so it can re-render only the affected region.
- **Focus model**: event dispatch to the focused component is not yet implemented. Current thinking: global handlers intercept first, then focused component's handlers.
- **Derived signals**: `ReadSignal.map` is deferred. Manual handler functions updating signals are sufficient for now.
- **Resize handling**: terminal resize events are not yet handled.
- **`effect.Dimensions` on Scala Native**: requires Posix `ioctl` — may be better to contribute upstream than implement in Terminus.
