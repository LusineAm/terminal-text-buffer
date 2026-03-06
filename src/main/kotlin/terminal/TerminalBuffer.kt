package terminal

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

    init {
        require(width > 0) { "width must be > 0 (was $width)" }
        require(height > 0) { "height must be > 0 (was $height)" }
        require(scrollbackMax >= 0) { "scrollbackMax must be >= 0 (was $scrollbackMax)" }

        screen = MutableList(height) { blankLine(width) }
        checkInvariants()
    }

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

    fun getCurrentAttributes(): Attributes = currentAttributes(currentFg, currentBg, currentStyle)

    fun getCursor(): Pair<Int, Int> = cursorCol to cursorRow

    fun setCursor(col: Int, row: Int) {
        cursorCol = clamp(col, 0, width - 1)
        cursorRow = clamp(row, 0, height - 1)
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
                '\n' -> newline()
                '\r' -> carriageReturn()
                else -> {
                    putCharAtCursor(ch)
                    advanceCursorAfterPrintable()
                }
            }
        }
    }

    fun insertText(text: String) {
        if (text.isEmpty()) return

        for (ch in text) {
            when (ch) {
                '\n' -> newline()
                '\r' -> carriageReturn()
                else -> {
                    val overflow = insertCellAt(cursorRow, cursorCol, makeCell(ch))
                    cascadeOverflow(cursorRow + 1, overflow)
                    advanceCursorAfterPrintable()
                }
            }
        }
    }

    fun fillCurrentLine(ch: Char? = null) {
        val fillChar = ch ?: ' '
        for (col in 0 until width) {
            screen[cursorRow][col] = makeCell(fillChar)
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

        screen.add(blankLine(width))

        cursorRow = clamp(cursorRow, 0, height - 1)
        cursorCol = clamp(cursorCol, 0, width - 1)
    }

    private fun putCharAtCursor(ch: Char) {
        screen[cursorRow][cursorCol] = makeCell(ch)
    }

    private fun makeCell(ch: Char): Cell = createCell(ch, currentFg, currentBg, currentStyle)

    private fun insertCellAt(row: Int, col: Int, newCell: Cell): Cell? {
        val line = screen[row]
        val dropped = line[width - 1]
        for (i in width - 1 downTo col + 1) {
            line[i] = line[i - 1]
        }
        line[col] = newCell
        return if (dropped == emptyCell()) null else dropped
    }

    private fun cascadeOverflow(startRow: Int, overflow: Cell?) {
        var row = startRow
        var pending = overflow

        while (pending != null) {
            if (row == height) {
                scrollUpOneLine()
                row = height - 1
            }
            pending = insertCellAt(row, 0, pending)
            row += 1
        }
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

    private fun newline() {
        cursorCol = 0
        cursorRow += 1
        if (cursorRow == height) {
            scrollUpOneLine()
        }
    }

    private fun carriageReturn() {
        cursorCol = 0
    }

    fun clearScreen() {
        val oldCursorCol = cursorCol
        val oldCursorRow = cursorRow

        screen.clear()
        repeat(height) {
            screen.add(blankLine(width))
        }

        cursorCol = clamp(oldCursorCol, 0, width - 1)
        cursorRow = clamp(oldCursorRow, 0, height - 1)
    }

    fun clearScreenAndScrollback() {
        clearScreen()
        scrollback.clear()
        cursorCol = 0
        cursorRow = 0
    }

    fun getCell(area: BufferArea, row: Int, col: Int): Cell =
        cellAt(area, row, col, width, height, screen, scrollback)

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
        val line = lineAt(area, row, height, screen, scrollback)
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

    fun getScreenCell(col: Int, row: Int): Cell {
        return getCell(BufferArea.SCREEN, row, col)
    }

    fun getScreenLineAsString(row: Int): String {
        return getLineString(BufferArea.SCREEN, row)
    }

    fun getScrollbackLineAsString(row: Int): String {
        return getLineString(BufferArea.SCROLLBACK, row)
    }
}
