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

        screen = MutableList(height) { createBlankLine() }
        checkInvariants()
    }

    private fun createBlankLine(): MutableList<Cell> = MutableList(width) { Cell() }

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
        TODO("API skeleton only")
    }

    fun insertText(text: String) {
        TODO("API skeleton only")
    }

    fun fillCurrentLine(ch: Char? = null) {
        TODO("API skeleton only")
    }

    fun insertEmptyLineAtBottom() {
        TODO("API skeleton only")
    }

    fun clearScreen() {
        TODO("API skeleton only")
    }

    fun clearScreenAndScrollback() {
        TODO("API skeleton only")
    }

    fun getScreenChar(col: Int, row: Int): Char {
        TODO("API skeleton only")
    }

    fun getScrollbackChar(col: Int, row: Int): Char {
        TODO("API skeleton only")
    }

    fun getScreenCell(col: Int, row: Int): Cell {
        TODO("API skeleton only")
    }

    fun getScrollbackCell(col: Int, row: Int): Cell {
        TODO("API skeleton only")
    }

    fun getScreenLineAsString(row: Int): String {
        TODO("API skeleton only")
    }

    fun getScrollbackLineAsString(row: Int): String {
        TODO("API skeleton only")
    }

    fun getEntireScreenAsString(): String {
        TODO("API skeleton only")
    }

    fun getEntireContentAsString(): String {
        TODO("API skeleton only")
    }
}