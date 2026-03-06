# Terminal Text Buffer (Kotlin)

This repository contains a simplified terminal screen buffer implementation written in Kotlin, developed as a technical exercise for terminal emulation behavior.

## Project Description
This project is a simplified terminal text buffer implementation in Kotlin.

It supports:
- a fixed-size visible `screen`
- bounded `scrollback` history
- cursor positioning and movement
- text writing (`writeText`) and insertion (`insertText`)
- per-cell attributes (`TermColor`, `TextStyle`, `Attributes`)
- clear operations (`clearScreen`, `clearScreenAndScrollback`)
- content access APIs for cells, chars, attributes, and string views

## Run Tests
Run all tests:

```bash
./gradlew test
```

Windows PowerShell:

```powershell
.\gradlew.bat test
```

Run a single test class:

```bash
./gradlew test --tests "terminal.WriteTextTest"
```

## Architecture Overview
Core model:
- `TerminalBuffer` is the main mutable model and behavior entry point.
- `Cell` stores `ch`, `fg`, `bg`, and `style`.
- `Attributes` groups foreground/background colors and `TextStyle`.
- `TextStyle` contains `bold`, `italic`, and `underline`.
- `TermColor` defines supported terminal colors (including `DEFAULT`).

Storage layout:
- `screen` is a `MutableList<MutableList<Cell>>` sized `height x width`.
- `scrollback` is a `MutableList<List<Cell>>` storing full-width lines up to `scrollbackMax`.

Behavior policy:
- Cursor movement is clamped to visible bounds.
- Text operations handle wrapping/newlines/carriage return and trigger upward scrolling when needed.
- Detailed behavior decisions are documented in [SPEC.md](SPEC.md).
