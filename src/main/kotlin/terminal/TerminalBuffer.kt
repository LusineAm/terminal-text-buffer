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
    var currentBold: Boolean = false
        private set

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
        TODO("API skeleton only")
    }

    fun getAttributes(): Attributes {
        TODO("API skeleton only")
    }

    fun getCursor(): Pair<Int, Int> {
        TODO("API skeleton only")
    }

    fun setCursor(col: Int, row: Int) {
        TODO("API skeleton only")
    }

    fun moveUp(n: Int) {
        TODO("API skeleton only")
    }

    fun moveDown(n: Int) {
        TODO("API skeleton only")
    }

    fun moveLeft(n: Int) {
        TODO("API skeleton only")
    }

    fun moveRight(n: Int) {
        TODO("API skeleton only")
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
