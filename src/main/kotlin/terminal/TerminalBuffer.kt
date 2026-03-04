package terminal

data class Attributes(
    val fg: TermColor = TermColor.DEFAULT,
    val bg: TermColor = TermColor.DEFAULT,
    val style: TextStyle = TextStyle()
)

class TerminalBuffer(
    val width: Int,
    val height: Int,
    val scrollbackMaxLines: Int
) {
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
