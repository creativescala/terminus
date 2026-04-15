# Terminus

Terminus is a Scala terminal toolkit, cross-platform across JVM, JS, and Scala Native. It is split into two main modules:

- **`core/`** — low-level effect traits and ANSI escape code primitives
- **`ui/`** — cell buffer–based terminal UI toolkit built on top of core

## Notes

Design notes and architectural decisions are kept in `notes/`:

- [`notes/ui-architecture.md`](notes/ui-architecture.md) — cell buffer model, component/layout system, effect layer, wide character support
- [`notes/styling-design.md`](notes/styling-design.md) — agreed design direction for styling (style split, no cascade, CSS box model, separators)

## Build

```
sbt compile
sbt test
sbt 'uiNative/runMain terminus.ui.demo'
```
