# Solution Notes

## Overview
This project implements a simplified terminal text buffer in Kotlin around the `TerminalBuffer` class.

The implementation includes:
- fixed-size visible screen storage
- bounded scrollback history
- clamped cursor positioning and movement
- overwrite (`writeText`) and insert (`insertText`) text operations
- per-cell styling through `TermColor`, `TextStyle`, and `Attributes`
- clear operations for screen-only and screen+scrollback
- content access APIs for cells/chars/attributes and string rendering

Behavior choices are aligned with [SPEC.md](SPEC.md), including newline/carriage-return handling, wrapping, scroll-up behavior, and trimming policy for string rendering.

## Trade-offs
- Fixed-width line storage (`height x width`) was chosen for predictable indexing and straightforward cell access.
- Scrollback stores full-width lines (`List<Cell>`) rather than diffs or runs; this is simpler and deterministic, at the cost of extra memory for sparse lines.
- Cursor movement and direct cursor set operations are clamped to visible bounds, which keeps behavior stable and avoids invalid internal states.
- Wrapping/newline behavior follows the SPEC strictly, including the boundary case where filling the last cell advances and may immediately scroll.
- `insertText` uses right-shift plus overflow cascading across following lines. This is easy to reason about but not the cheapest possible approach for large inserts.
- The API stays concrete and direct (`TerminalBuffer` with helper functions) instead of introducing additional abstraction layers early.

## Time Complexity
- `setCursor`, `moveUp/Down/Left/Right`, `getCursor`: O(1)
- `getCell`, `getChar`, `getAttributes`: O(1)
- `fillCurrentLine`: O(width)
- `clearScreen`: O(width * height)
- `clearScreenAndScrollback`: O(width * height + scrollback size)
- `getLineString`: O(width)
- `getScreenAsString`: O(width * height)
- `getAllAsString`: O(width * (height + scrollback size))
- `writeText`: proportional to input length, with extra cost when wrapping/newline triggers scroll (scroll operation touches line structures and creates a new blank line)
- `insertText`: proportional to inserted text length, with additional per-character shifting/cascading work across lines; worst cases are noticeably heavier than `writeText`

## Memory:
- Screen storage is `width * height` cells.
- Scrollback stores up to `scrollbackMax` full lines of width cells.

## Specification
Behavior that can vary between terminal implementations (e.g., wrapping rules, trimming policy for string rendering, cursor behavior after clearing) is documented in SPEC.md.
Tests are written to match the documented specification,
ensuring deterministic and reviewable behavior.

## Use of AI Assistance

During implementation of this task I used AI tools as development aid.  
They helped speed up some parts of the work, such as exploring implementation approaches,
drafting code ideas, and generating additional test scenarios.

All suggestions were reviewed and adapted manually, and the final behavior was verified
against the rules described in `SPEC.md`. The tests were used to confirm that the buffer
behaves consistently for normal cases, edge cases, and boundary conditions.

AI was mainly used as a productivity tool during development, while design decisions,
behavior specification, and final implementation were validated and refined through manual work.