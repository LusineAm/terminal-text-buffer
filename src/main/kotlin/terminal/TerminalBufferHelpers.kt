package terminal

internal fun clamp(value: Int, min: Int, max: Int): Int {
    return when {
        value < min -> min
        value > max -> max
        else -> value
    }
}

internal fun currentAttributes(fg: TermColor, bg: TermColor, style: TextStyle): Attributes {
    return Attributes(
        fg = fg,
        bg = bg,
        style = style.copy()
    )
}

internal fun createCell(ch: Char, fg: TermColor, bg: TermColor, style: TextStyle): Cell {
    return Cell(
        ch = ch,
        fg = fg,
        bg = bg,
        style = style.copy()
    )
}

internal fun emptyCell(attributes: Attributes = Attributes()): Cell {
    return createCell(' ', attributes.fg, attributes.bg, attributes.style)
}

internal fun blankLine(width: Int, attributes: Attributes = Attributes()): MutableList<Cell> {
    return MutableList(width) { emptyCell(attributes) }
}

internal fun copyLine(line: List<Cell>): List<Cell> =
    line.map { it.copy(style = it.style.copy()) }
