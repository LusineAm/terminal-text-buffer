package terminal

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FillCurrentLineTest {

    @Test
    fun fillCurrentLine_fills_every_column_on_cursor_row_only() {
        val buffer = TerminalBuffer(width = 5, height = 3, scrollbackMax = 10)
        buffer.setScreenRow(0, "abcde")
        buffer.setScreenRow(1, ".....")
        buffer.setScreenRow(2, "12345")
        buffer.setCursor(3, 1)

        buffer.fillCurrentLine('X')

        assertRowChars(buffer, row = 0, expected = "abcde")
        assertRowChars(buffer, row = 1, expected = "XXXXX")
        assertRowChars(buffer, row = 2, expected = "12345")
    }

    @Test
    fun fillCurrentLine_uses_current_attributes() {
        val buffer = TerminalBuffer(width = 5, height = 3, scrollbackMax = 10)
        val style = TextStyle(bold = true, underline = true)
        buffer.setAttributes(TermColor.RED, TermColor.BLUE, style)
        buffer.setCursor(2, 1)

        buffer.fillCurrentLine('A')

        for (col in 0 until buffer.width) {
            assertEquals('A', buffer.getChar(BufferArea.SCREEN, 1, col))
            assertEquals(
                Attributes(
                    fg = TermColor.RED,
                    bg = TermColor.BLUE,
                    style = style
                ),
                buffer.getAttributes(BufferArea.SCREEN, 1, col)
            )
        }
    }

    @Test
    fun fillCurrentLine_default_argument_fills_spaces_with_current_attributes() {
        val buffer = TerminalBuffer(width = 5, height = 3, scrollbackMax = 10)
        val style = TextStyle(italic = true)
        buffer.setAttributes(TermColor.GREEN, TermColor.BLACK, style)
        buffer.setCursor(4, 0)

        buffer.fillCurrentLine()

        for (col in 0 until buffer.width) {
            assertEquals(' ', buffer.getChar(BufferArea.SCREEN, 0, col))
            assertEquals(
                Attributes(
                    fg = TermColor.GREEN,
                    bg = TermColor.BLACK,
                    style = style
                ),
                buffer.getAttributes(BufferArea.SCREEN, 0, col)
            )
        }
    }

    @Test
    fun fillCurrentLine_does_not_change_scrollback() {
        val buffer = TerminalBuffer(width = 4, height = 3, scrollbackMax = 10)
        buffer.setScreenRow(0, "ABCD")
        buffer.setScreenRow(1, "EFGH")
        buffer.setScreenRow(2, "IJKL")
        buffer.insertEmptyLineAtBottom()
        val expectedScrollback = buffer.scrollback.map { line -> line.map { it.copy(style = it.style.copy()) } }

        buffer.setCursor(1, 1)
        buffer.fillCurrentLine('Z')

        assertEquals(expectedScrollback, buffer.scrollback)
    }

    @Test
    fun fillCurrentLine_keeps_cursor_position() {
        val buffer = TerminalBuffer(width = 5, height = 3, scrollbackMax = 10)
        buffer.setCursor(4, 1)
        val expectedCursor = buffer.getCursor()

        buffer.fillCurrentLine('Q')

        assertEquals(expectedCursor, buffer.getCursor())
    }

    @Test
    fun fillCurrentLine_works_on_first_and_last_rows() {
        val buffer = TerminalBuffer(width = 5, height = 3, scrollbackMax = 10)

        buffer.setCursor(0, 0)
        buffer.fillCurrentLine('T')
        assertRowChars(buffer, row = 0, expected = "TTTTT")

        buffer.setCursor(0, 2)
        buffer.fillCurrentLine('B')
        assertRowChars(buffer, row = 2, expected = "BBBBB")
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
}
