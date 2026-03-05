# Terminal Text Buffer Spec

## 1. Coordinate system

- The cursor uses 0-based coordinates in `(col, row)` order.
- Valid cursor bounds are clamped to the visible screen rectangle:
  - `col` in `0..width - 1`
  - `row` in `0..height - 1`

## 2. Data model

- The screen is a fixed grid of `height` lines, each containing `width` cells.
- Each cell stores:
  - a character
  - foreground color
  - background color
  - style flags
- The default cell is:
  - `ch = ' '`
  - `fg = DEFAULT`
  - `bg = DEFAULT`
  - `bold = false`, `italic = false`, `underline = false`

## 3. String rendering policy

- When converting a single line to `String`, trailing spaces on the right are removed with `trimEnd()`.
- Multi-line rendering joins lines with `'\n'`.
- "Entire content" means `scrollback` lines first, then visible `screen` lines.

## 4. Scrollback

- Scrollback stores full logical lines, each with exactly `width` cells.
- When a line scrolls off the top of the screen, it is appended to scrollback and becomes immutable history.
- Scrollback keeps at most `scrollbackMaxLines` lines.
- If scrollback exceeds that limit, the oldest lines are dropped first.

## 5. Newline and wrapping

- Writing a normal printable character writes into the current cursor cell, then advances the cursor one column to the right.
- If a printable character is written in the last column, that character stays in place and the next write position wraps to the first column of the next row.
- `'\n'` moves the cursor to column `0` of the next row without writing a visible character.
- `'\r'` moves the cursor to column `0` of the current row without changing any cells.
- If wrapping or `'\n'` moves the cursor past the bottom row, `scrollUpOne` behavior is triggered:
  - the top visible screen line moves into scrollback
  - all remaining screen lines shift up by one
  - a new blank line is inserted at the bottom
  - after scroll, cursor row becomes `height - 1`
  - the cursor ends on the bottom row at the computed column (`0` for wrap/newline)

## 6. Editing operation semantics

- `writeText` overwrites cells starting at the cursor, uses current attributes, advances the cursor, wraps across lines, and scrolls when advancing past the bottom.
- `insertText` inserts starting at the cursor by shifting existing cells to the right, cascades overflow into following lines, uses current attributes, and may wrap and scroll.
- `fillCurrentLine` fills the entire cursor row using current attributes. If `ch` is `null`, it uses the default blank character `' '`.
- `clearScreen` resets only the visible screen to default cells and preserves scrollback.
- `clearAll` semantics are exposed by `clearScreenAndScrollback`: both the visible screen and scrollback are reset.
- After `clearScreen` the cursor position is preserved (clamped to screen bounds).
- After `clearScreenAndScrollback`, the cursor becomes `(0, 0)`.
- `clearScreen` and `clearScreenAndScrollback` preserve current attributes.

## 7. Content access

- Indexing policy: content access methods throw `IndexOutOfBoundsException` when `(col, row)` is out of bounds.
