# Styling Design

## Style split

Three distinct levels:

- **Cell-level `Style`** — color, bold, italic, underline, etc. Used everywhere, passed directly to `Buffer.putString` / `Buffer.put`. Already implemented.
- **Component-level style** — padding, border, background fill. Parameters on the component itself, not on `Style`.
- **Parent-context style** — gutters, alignment modes. Expressed through a more specific `RenderContext` subtype, only accessible when the parent provides it (compile-time enforcement).

## No cascade

No CSS-style cascade. Scala provides sufficient abstraction (`Style` is a case class, `.copy()` works, shared styles are plain `val`s or functions). A cascade would add action-at-a-distance complexity for no gain. If ambient style context is ever needed, a `using` parameter is the right mechanism.

## CSS box model as reference

Content → padding → border → margin is a good foundation. Well understood, maps cleanly to terminal cells. Copying it saves users learning a new mental model.

## Component-specific styles

Different components may have style properties that don't apply universally. Example: `Columns`/`Grid` could expose gutter width to children via a typed `RenderContext` subtype. A child that tries to specify a gutter in a plain `Column` context would be a compile error — unlike CSS where unknown properties silently do nothing.

## Labelled borders

TUIs commonly embed text in a border (e.g. a title or status in the top edge of a box — as seen in Claude Code's input panel). This is a string spliced into the horizontal run of the border, replacing some `─` characters. Alignment (left/centre/right) and width clamping are the main concerns. This lives in `Box.render` and is deferred to later work.

## Separators vs borders

Separators (horizontal/vertical lines *between* children) are structurally different from borders (owned by a single component). Separators are a **rendering policy of the parent container**, not a child component. A `Column` or `Row` with separators owns the decision and knows which join characters to use (┳ ┻ ┣ ┫ etc.) based on position. Children don't need to know about them. Junction resolution between child borders and parent separators is deferred — keep simple initially.

## Block vs inline (deferred)

The current `Text` component conflates a block element (box with border, background fill, padding) and an inline element (styled text content). This means `content: Style` has a `bg` field that can diverge from `ComponentStyle.background`, requiring the user to keep them in sync manually.

The clean resolution: content style `bg = Color.Default` means "transparent — inherit from the component background", matching CSS's default behaviour. An explicit `bg` on a content style is an intentional override (like a highlighted `<span>`). This requires a concept of cell-style transparency/inheritance, which is deferred until inline/span styling is tackled.

## Implementation order

Rough priority for immediate work:
1. Fill style (background colour for entire component area, including empty cells)
2. Separate box-border style from content style in `Text`
3. Padding
4. Separators (later, as a parent container feature)
5. Alignment / gutter (later, tied to dynamic layout and typed `RenderContext`)
