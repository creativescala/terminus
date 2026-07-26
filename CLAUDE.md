# Terminus

Terminus is a Scala terminal toolkit, cross-platform across JVM, JS, and Scala Native. It is split into two main modules:

- **`core/`** — low-level effect traits and ANSI escape code primitives
- **`core-ce/`** — Cats Effect integration for core: char pump, CE key driver
- **`ui/`** — cell buffer–based terminal UI toolkit built on top of core, with a Cats Effect event-queue runner (`terminus.ui.ce`) providing the `Timer` capability

## Notes

Design notes and architectural decisions are kept in `notes/`:

- [`notes/ui-architecture.md`](notes/ui-architecture.md) — cell buffer model, component/layout system, effect layer, wide character support
- [`notes/styling-design.md`](notes/styling-design.md) — agreed design direction for styling (style split, no cascade, CSS box model, separators)
- [`notes/event-queue-runtime.md`](notes/event-queue-runtime.md) — agreed design for #47: unified event queue, blocking + Cats Effect runners, `core-ce` module and `ui`'s `ce` package, `Timer` capability

## Build

```
sbt compile
sbt test
sbt 'uiNative/runMain terminus.ui.demo'
sbt 'uiJVM/runMain terminus.ui.ce.demo'    # CE runner: spinner/timers demo
```
