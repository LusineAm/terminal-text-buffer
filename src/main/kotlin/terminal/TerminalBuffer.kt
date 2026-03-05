package terminal

data class Attributes(
    val fg: TermColor = TermColor.DEFAULT,
    val bg: TermColor = TermColor.DEFAULT,
    val style: TextStyle = TextStyle()
)

class TerminalBuffer(
    val width: Int,
    val height: Int,
    val scrollbackMax: Int
) {
    val screen: MutableList<MutableList<Cell>>
    val scrollback: MutableList<List<Cell>> = mutableListOf()

    var cursorCol: Int = 0
        private set
    var cursorRow: Int = 0
        private set

    var currentFg: TermColor = TermColor.DEFAULT
        private set
    var currentBg: TermColor = TermColor.DEFAULT
        private set

    private var currentStyle: TextStyle = TextStyle()

    val currentBold: Boolean
        get() = currentStyle.bold

    init {
        require(width > 0) { "width must be > 0 (was $width)" }
        require(height > 0) { "height must be > 0 (was $height)" }
        require(scrollbackMax >= 0) { "scrollbackMax must be >= 0 (was $scrollbackMax)" }

        screen = MutableList(height) { blankLine() }
        checkInvariants()
    }

    private fun defaultCell(): Cell = Cell()

    private fun blankLine(): MutableList<Cell> = MutableList(width) { defaultCell() }

    private fun blankLineUsingCurrentAttributesOrDefault(): MutableList<Cell> = blankLine()

    private fun checkInvariants() {
        check(screen.size == height) { "screen size must equal height ($height, was ${screen.size})" }
        check(screen.all { it.size == width }) { "each screen line must have width cells ($width)" }
        check(cursorRow in 0 until height) { "cursorRow out of bounds (row=$cursorRow, height=$height)" }
        check(cursorCol in 0 until width) { "cursorCol out of bounds (col=$cursorCol, width=$width)" }
    }

    fun setAttributes(fg: TermColor, bg: TermColor, style: TextStyle) {
        currentFg = fg
        currentBg = bg
        currentStyle = style.copy()
    }

    fun getCurrentAttributes(): Attributes {
        return Attributes(
            fg = currentFg,
            bg = currentBg,
            style = currentStyle.copy()
        )
    }

    fun getAttributes(): Attributes = getCurrentAttributes()

    fun getCursor(): Pair<Int, Int> = cursorCol to cursorRow

    fun setCursor(col: Int, row: Int) {
        cursorCol = col.coerceIn(0, width - 1)
        cursorRow = row.coerceIn(0, height - 1)
    }

    fun moveUp(n: Int) {
        if (n <= 0) return
        setCursor(cursorCol, cursorRow - n)
    }

    fun moveDown(n: Int) {
        if (n <= 0) return
        setCursor(cursorCol, cursorRow + n)
    }

    fun moveLeft(n: Int) {
        if (n <= 0) return
        setCursor(cursorCol - n, cursorRow)
    }

    fun moveRight(n: Int) {
        if (n <= 0) return
        setCursor(cursorCol + n, cursorRow)
    }

    fun writeText(text: String) {
        if (text.isEmpty()) return

        for (ch in text) {
            when (ch) {
                '\n' -> newLine()
                '\r' -> carriageReturn()
                else -> {
                    putCharAtCursor(ch)
                    advanceCursorAfterPrintable()
                }
            }
        }
    }

    fun insertText(text: String) {
        TODO("API skeleton only")
    }

    fun fillCurrentLine(ch: Char? = null) {
        check(cursorRow in 0 until height) { "cursorRow out of bounds (row=$cursorRow, height=$height)" }

        val fillChar = ch ?: ' '
        for (col in 0 until width) {
            screen[cursorRow][col] = Cell(
                ch = fillChar,
                fg = currentFg,
                bg = currentBg,
                style = currentStyle.copy()
            )
        }
    }

    fun insertEmptyLineAtBottom() {
        scrollUpOneLine()
    }

    private fun scrollUpOneLine() {
        if (screen.isEmpty()) return

        val removedTopLine = screen.removeAt(0)

        if (scrollbackMax > 0) {
            scrollback.add(copyLine(removedTopLine))
            while (scrollback.size > scrollbackMax) {
                scrollback.removeAt(0)
            }
        }

        screen.add(blankLineUsingCurrentAttributesOrDefault())

        cursorRow = cursorRow.coerceIn(0, height - 1)
        cursorCol = cursorCol.coerceIn(0, width - 1)
    }

    private fun putCharAtCursor(ch: Char) {
        screen[cursorRow][cursorCol] = Cell(
            ch = ch,
            fg = currentFg,
            bg = currentBg,
            style = currentStyle.copy()
        )
    }

    private fun advanceCursorAfterPrintable() {
        cursorCol += 1
        if (cursorCol == width) {
            cursorCol = 0
            cursorRow += 1
            if (cursorRow == height) {
                scrollUpOneLine()
            }
        }
    }

    private fun newLine() {
        cursorCol = 0
        cursorRow += 1
        if (cursorRow == height) {
            scrollUpOneLine()
        }
    }

    private fun carriageReturn() {
        cursorCol = 0
    }

    private fun copyLine(line: List<Cell>): List<Cell> =
        line.map { it.copy(style = it.style.copy()) }

    private fun line(area: BufferArea, row: Int): List<Cell> {
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

    private fun cell(area: BufferArea, row: Int, col: Int): Cell {
        val line = line(area, row)
        if (col !in 0 until width) {
            throw IndexOutOfBoundsException("column out of bounds (col=$col, width=$width)")
        }
        return line[col]
    }

    fun clearScreen() {
        val oldCursorCol = cursorCol
        val oldCursorRow = cursorRow

        screen.clear()
        repeat(height) {
            screen.add(blankLine())
        }

        cursorCol = oldCursorCol.coerceIn(0, width - 1)
        cursorRow = oldCursorRow.coerceIn(0, height - 1)
    }

    fun clearScreenAndScrollback() {
        clearScreen()
        scrollback.clear()
        cursorCol = 0
        cursorRow = 0
    }

    fun getCell(area: BufferArea, row: Int, col: Int): Cell = cell(area, row, col)

    fun getChar(area: BufferArea, row: Int, col: Int): Char = getCell(area, row, col).ch

    fun getAttributes(area: BufferArea, row: Int, col: Int): Attributes {
        val cell = getCell(area, row, col)
        return Attributes(
            fg = cell.fg,
            bg = cell.bg,
            style = cell.style.copy()
        )
    }

    fun getLineString(area: BufferArea, row: Int): String {
        val line = line(area, row)
        val chars = CharArray(width) { col -> line[col].ch }
        return String(chars).trimEnd()
    }

    fun getScreenAsString(): String =
        (0 until height).joinToString(separator = "\n") { row -> getLineString(BufferArea.SCREEN, row) }

    fun getAllAsString(): String {
        val lines = mutableListOf<String>()
        for (row in 0 until scrollback.size) {
            lines.add(getLineString(BufferArea.SCROLLBACK, row))
        }
        for (row in 0 until height) {
            lines.add(getLineString(BufferArea.SCREEN, row))
        }
        return lines.joinToString(separator = "\n")
    }

    fun getScreenChar(col: Int, row: Int): Char {
        return getChar(BufferArea.SCREEN, row, col)
    }

    fun getScrollbackChar(col: Int, row: Int): Char {
        return getChar(BufferArea.SCROLLBACK, row, col)
    }

    fun getScreenCell(col: Int, row: Int): Cell {
        return getCell(BufferArea.SCREEN, row, col)
    }

    fun getScrollbackCell(col: Int, row: Int): Cell {
        return getCell(BufferArea.SCROLLBACK, row, col)
    }

    fun getScreenLineAsString(row: Int): String {
        return getLineString(BufferArea.SCREEN, row)
    }

    fun getScrollbackLineAsString(row: Int): String {
        return getLineString(BufferArea.SCROLLBACK, row)
    }

    fun getEntireScreenAsString(): String {
        return getScreenAsString()
    }

    fun getEntireContentAsString(): String {
        return getAllAsString()
    }
}