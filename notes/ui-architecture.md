# UI Module Architecture

## Overview

The `ui/` module builds a terminal UI toolkit on top of the core effect layer. Rendering uses a **cell buffer** model: components write into an in-memory 2D grid of cells, then a single controlled flush to the terminal at the end of each frame.

## Key types

- **`Cell(codePoint: Int, style: Style)`** — a single terminal cell. `codePoint = 0` is the continuation sentinel for the right half of wide characters.
- **`Buffer(width, height)`** — row-major flat array of cells. Out-of-bounds writes are silently ignored (component isolation). Key methods: `put`, `fill`, `putString`, `render`, `renderDiff`.
- **`Rect(x, y, width, height)`** — 0-based position + size. 1-based conversion happens only inside `Buffer.render`.
- **`Style`** — cell-level attributes: fg/bg color, bold, italic, underline, blink, invert, strikethrough.
- **`Component`** — trait with `size: Size` and `render(bounds: Rect, buf: Buffer): Unit`.
- **`RenderContext`** — trait that components add children to. `RootContext` flushes to terminal; `ChildContext` renders into a buffer region.

## Layout

Two-phase: layout pass (size accumulation via `add`) then render pass (writing to buffer via `render`).

- **`Row`** — horizontal layout, accumulates x offsets.
- **`Column`** — vertical layout, accumulates y offsets.
- **`FullScreen`** — root context, creates buffer sized to content (not terminal dimensions — avoids `effect.Dimensions` which is not implemented on Scala Native).

## Effect layer

`terminus.ui.Terminal = effect.AlternateScreenMode & effect.Erase & effect.Color & effect.Cursor & effect.Format & effect.Writer`

Components do not receive `Terminal`. Only `Buffer.render` / `Buffer.renderDiff` touch the terminal.

## Wide character support

`CharWidth.of(codePoint)` returns 0, 1, or 2 per Unicode TR#11. Wide characters occupy two cells: left cell holds the character, right cell holds `Cell.continuation`. `putString` handles this automatically.

## Double buffering

`Buffer.renderDiff(previous)` compares two same-sized buffers and emits only changed cells, with cursor-position tracking to suppress redundant `cursor.to` calls for adjacent changes.

## Deferred work

- `effect.Dimensions` on Scala Native (requires Posix `ioctl` — may be better to contribute upstream than implement in Terminus).
