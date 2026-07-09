# Styling Design

## Stateful styles

A component style is a function from the component's state to the resolved
properties it renders with, represented as data: `Style[State, Props]` is a
base `Props` plus an ordered list of rules `(State => Boolean, Props => Props)`.

- **Props** is the flat, resolved property set (e.g. `TextProps(box, content)`)
  — a plain case class, no mixins.
- **State** is a record of the component's state variables
  (`ComponentState(focus, availability)`), with traits (`HasFocus`,
  `HasAvailability`) mirroring the capabilities.
- **Rules are patches over the base, not replacements.** A focused rule that
  only sets bold keeps every other base property. Patches are endomorphisms
  `Props => Props` under composition — the reification of the "merge over
  base" monoid, but strictly more expressive than per-field `Option` merging
  because a patch can express relative changes ("bold, whatever the base is").
- **Rules stack in declaration order; later wins on conflict.** Combined
  states (disabled *and* focused) get both patches, so there is no hardcoded
  precedence in components.
- **State-specific combinators are typed.** `focused` / `disabled` are
  extension methods constrained by the state traits, so styling a state a
  component doesn't have is a compile error — same philosophy as the typed
  `LayoutContext` below.

Components resolve the active properties with `style(context.state)` (see
`DefaultEvent.state`); both `measure` and `render` use the same state
snapshot. Element-level states (e.g. the highlighted item in `Select`) are
not covered by the component-level state record; the same `Style` shape can
be reused there with a per-item state record when needed.

## Naming: Style vs Props

The type names follow the model. A **`*Props`** type is a resolved record of
properties — plain data, no state, no functions (`CellProps`, `BoxProps`,
`LayoutProps`, and the per-component records below). A **`*Style`** type is a
function from component state to props — `Style[State, Props]` or an alias of
it. Anything that holds resolved properties is named props (`Cell.props`,
`BoxProps.borderProps`), reserving "style" for the state-selecting layer.

## Per-component styles

Each stateful component has its own props record and style alias:
`TextProps`/`TextStyle`, `LineProps`/`LineStyle`, `ButtonProps`/`ButtonStyle`,
`SelectProps`/`SelectStyle`, `TextInputProps`/`TextInputStyle`. Structural
duplication between them (most have `box` and `content`) is accepted: it keeps
each component free to evolve its own properties, and the work lands on the
framework developer once rather than on users. Component-specific slots live
in the props record — `SelectProps.selected` (the properties of the selected
item, default inverted) and `TextInputProps.cursor` (the cursor cell, default
inverted). These slots are plain `CellProps`, not patches over `content`:
props stay pure data, and relative styling belongs in `Style` rules.
`Column`/`Row` take a plain `LayoutProps` — they have no state, so there is
nothing for a `Style` to select on.

## Style split

Three distinct levels:

- **Cell-level `CellProps`** — color, bold, italic, underline, etc. Used everywhere, passed directly to `Buffer.putString` / `Buffer.put`. Already implemented.
- **Component-level style** — padding, border, background fill. Parameters on the component itself, not on `CellProps`.
- **Parent-context style** — gutters, alignment modes. Expressed through a more specific `LayoutContext` subtype, only accessible when the parent provides it (compile-time enforcement).

## No cascade

No CSS-style cascade. Scala provides sufficient abstraction (`CellProps` is a case class, `.copy()` works, shared styles are plain `val`s or functions). A cascade would add action-at-a-distance complexity for no gain. If ambient style context is ever needed, a `using` parameter is the right mechanism.

## CSS box model as reference

Content → padding → border → margin is a good foundation. Well understood, maps cleanly to terminal cells. Copying it saves users learning a new mental model.

## Component-specific styles

Different components may have style properties that don't apply universally. Example: `Columns`/`Grid` could expose gutter width to children via a typed `LayoutContext` subtype. A child that tries to specify a gutter in a plain `Column` context would be a compile error — unlike CSS where unknown properties silently do nothing.

## Labelled borders

TUIs commonly embed text in a border (e.g. a title or status in the top edge of a box — as seen in Claude Code's input panel). This is a string spliced into the horizontal run of the border, replacing some `─` characters. Alignment (left/centre/right) and width clamping are the main concerns. This lives in `Box.render` and is deferred to later work.

## Separators vs borders

Separators (horizontal/vertical lines *between* children) are structurally different from borders (owned by a single component). Separators are a **rendering policy of the parent container**, not a child component. A `Column` or `Row` with separators owns the decision and knows which join characters to use (┳ ┻ ┣ ┫ etc.) based on position. Children don't need to know about them. Junction resolution between child borders and parent separators is deferred — keep simple initially.

## Block vs inline (deferred)

The current `Text` component conflates a block element (box with border, background fill, padding) and an inline element (styled text content). This means `content: CellProps` has a `bg` field that can diverge from `BoxProps.background`, requiring the user to keep them in sync manually.

The clean resolution: content style `bg = Color.Default` means "transparent — inherit from the component background", matching CSS's default behaviour. An explicit `bg` on a content style is an intentional override (like a highlighted `<span>`). This requires a concept of cell-style transparency/inheritance, which is deferred until inline/span styling is tackled.

## Implementation order

Rough priority for immediate work:
1. Fill style (background colour for entire component area, including empty cells)
2. Separate box-border style from content style in `Text`
3. Padding
4. Separators (later, as a parent container feature)
5. Alignment / gutter (later, tied to dynamic layout and typed `LayoutContext`)
