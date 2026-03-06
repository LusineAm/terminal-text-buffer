package terminal

internal fun lineAt(
    area: BufferArea,
    row: Int,
    height: Int,
    screen: List<List<Cell>>,
    scrollback: List<List<Cell>>
): List<Cell> {
    return when (area) {
        BufferArea.SCREEN -> {
            if (row !in 0 until height) {
                throw IndexOutOfBoundsException("screen row out of bounds (row=$row, height=$height)")
            }
            screen[row]
        }

        BufferArea.SCROLLBACK -> {
            if (row !in 0 until scrollback.size) {
                throw IndexOutOfBoundsException(
                    "scrollback row out of bounds (row=$row, size=${scrollback.size})"
                )
            }
            scrollback[row]
        }
    }
}

internal fun cellAt(
    area: BufferArea,
    row: Int,
    col: Int,
    width: Int,
    height: Int,
    screen: List<List<Cell>>,
    scrollback: List<List<Cell>>
): Cell {
    val line = lineAt(area, row, height, screen, scrollback)
    if (col !in 0 until width) {
        throw IndexOutOfBoundsException("column out of bounds (col=$col, width=$width)")
    }
    return line[col]
}
