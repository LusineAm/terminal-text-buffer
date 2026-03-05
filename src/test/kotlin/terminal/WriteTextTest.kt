package terminal

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class WriteTextTest {

    @Test
    fun writeText_overwrites_existing_content_and_preserves_unwritten_cells() {
        val buffer = TerminalBuffer(width = 6, height = 2, scrollbackMax = 10)
        buffer.setScreenRow(0, "abcdef")
        buffer.setScreenRow(1, "ghijkl")
        val untouched = copyCell(buffer.screen[0][4])

        val style = TextStyle(bold = true, underline = true)
        buffer.setAttributes(TermColor.RED, TermColor.BLUE, style)
        buffer.setCursor(1, 0)

        buffer.writeText("XYZ")

        assertRowChars(buffer, row = 0, expected = "aXYZef")
        assertEquals(0, buffer.scrollback.size)
        assertEquals(4 to 0, buffer.getCursor())
        assertEquals(untouched, buffer.screen[0][4])
        for (col in 1..3) {
            assertEquals(
                Attributes(
                    fg = TermColor.RED,
                    bg = TermColor.BLUE,
                    style = style
                ),
                buffer.getAttributes(BufferArea.SCREEN, 0, col)
            )
        }
    }

    @Test
    fun writeText_wraps_at_end_of_line() {
        val buffer = TerminalBuffer(width = 5, height = 3, scrollbackMax = 10)
        buffer.setCursor(4, 0)

        buffer.writeText("AB")

        assertEquals('A', buffer.getChar(BufferArea.SCREEN, 0, 4))
        assertEquals('B', buffer.getChar(BufferArea.SCREEN, 1, 0))
        assertEquals(1 to 1, buffer.getCursor())
    }

    @Test
    fun writeText_scrolls_when_writing_beyond_bottom_row() {
        val buffer = TerminalBuffer(width = 3, height = 2, scrollbackMax = 10)

        buffer.writeText("abcdefg")

        assertEquals(1, buffer.scrollback.size)
        assertEquals(3, buffer.scrollback[0].size)
        assertEquals("abc", buffer.scrollback[0].toChars())
        assertRowChars(buffer, row = 0, expected = "def")
        assertRowChars(buffer, row = 1, expected = "g  ")
        assertEquals(1 to 1, buffer.getCursor())
    }

    @Test
    fun writeText_newline_moves_to_next_line_and_does_not_write_visible_char() {
        val buffer = TerminalBuffer(width = 6, height = 3, scrollbackMax = 10)

        buffer.writeText("ab\ncd")

        assertRowChars(buffer, row = 0, expected = "ab    ")
        assertRowChars(buffer, row = 1, expected = "cd    ")
        assertEquals(2 to 1, buffer.getCursor())
    }

    @Test
    fun writeText_carriage_return_sets_column_to_zero() {
        val buffer = TerminalBuffer(width = 5, height = 2, scrollbackMax = 10)

        buffer.writeText("ab\rZ")

        assertRowChars(buffer, row = 0, expected = "Zb   ")
        assertEquals(1 to 0, buffer.getCursor())
    }

    @Test
    fun writeText_empty_string_is_no_op() {
        val buffer = TerminalBuffer(width = 4, height = 2, scrollbackMax = 10)
        buffer.setScreenRow(0, "ABCD")
        buffer.setScreenRow(1, "EFGH")
        buffer.insertEmptyLineAtBottom()
        buffer.setCursor(2, 1)

        val screenBefore = deepCopyLines(buffer.screen)
        val scrollbackBefore = deepCopyLines(buffer.scrollback)
        val cursorBefore = buffer.getCursor()

        buffer.writeText("")

        assertEquals(screenBefore, deepCopyLines(buffer.screen))
        assertEquals(scrollbackBefore, deepCopyLines(buffer.scrollback))
        assertEquals(cursorBefore, buffer.getCursor())
    }

    @Test
    fun writeText_uses_current_attributes() {
        val buffer = TerminalBuffer(width = 4, height = 2, scrollbackMax = 10)
        val style = TextStyle(bold = true)
        buffer.setAttributes(TermColor.RED, TermColor.BLUE, style)

        buffer.writeText("A")

        assertEquals(
            Cell(ch = 'A', fg = TermColor.RED, bg = TermColor.BLUE, style = style),
            buffer.getCell(BufferArea.SCREEN, row = 0, col = 0)
        )
    }

    @Test
    fun writeText_newline_on_bottom_row_scrolls_and_keeps_cursor_on_last_row() {
        val buffer = TerminalBuffer(width = 3, height = 2, scrollbackMax = 10)
        buffer.setScreenRow(0, "AAA")
        buffer.setScreenRow(1, "BBB")
        buffer.setCursor(0, 1)

        buffer.writeText("\n")

        assertEquals(1, buffer.scrollback.size)
        assertEquals("AAA", buffer.scrollback[0].toChars())
        assertRowChars(buffer, row = 0, expected = "BBB")
        assertRowChars(buffer, row = 1, expected = "   ")
        assertEquals(0 to 1, buffer.getCursor())
    }

    @Test
    fun writeText_scrollback_cap_zero_drops_scrolled_lines_immediately() {
        val buffer = TerminalBuffer(width = 2, height = 2, scrollbackMax = 0)

        buffer.writeText("abcde")

        assertEquals(0, buffer.scrollback.size)
        assertRowChars(buffer, row = 0, expected = "cd")
        assertRowChars(buffer, row = 1, expected = "e ")
        assertEquals(1 to 1, buffer.getCursor())
    }

    private fun TerminalBuffer.setScreenRow(row: Int, text: String) {
        require(text.length == width) { "text length must equal width" }
        for (col in 0 until width) {
            screen[row][col] = Cell(ch = text[col])
        }
    }

    private fun assertRowChars(buffer: TerminalBuffer, row: Int, expected: String) {
        require(expected.length == buffer.width) { "expected length must equal width" }
        for (col in 0 until buffer.width) {
            assertEquals(expected[col], buffer.getChar(BufferArea.SCREEN, row, col))
        }
    }

    private fun List<Cell>.toChars(): String = joinToString(separator = "") { it.ch.toString() }

    private fun copyCell(cell: Cell): Cell = cell.copy(style = cell.style.copy())

    private fun deepCopyLines(lines: List<List<Cell>>): List<List<Cell>> =
        lines.map { line -> line.map { copyCell(it) } }
}
