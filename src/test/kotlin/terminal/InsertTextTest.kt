package terminal

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class InsertTextTest {

    @Test
    fun insertText_empty_string_is_no_op() {
        val buffer = TerminalBuffer(width = 5, height = 2, scrollbackMax = 10)
        buffer.setScreenRow(0, "ABCDE")
        buffer.setScreenRow(1, "FGHIJ")
        buffer.setCursor(2, 1)

        val screenBefore = deepCopyLines(buffer.screen)
        val scrollbackBefore = deepCopyLines(buffer.scrollback)
        val cursorBefore = buffer.getCursor()

        buffer.insertText("")

        assertEquals(screenBefore, deepCopyLines(buffer.screen))
        assertEquals(scrollbackBefore, deepCopyLines(buffer.scrollback))
        assertEquals(cursorBefore, buffer.getCursor())
    }

    @Test
    fun insertText_shifts_right_on_same_line() {
        val buffer = TerminalBuffer(width = 5, height = 2, scrollbackMax = 10)
        buffer.writeText("abc")
        buffer.setCursor(1, 0)

        buffer.insertText("X")

        assertEquals('a', buffer.getScreenChar(0, 0))
        assertEquals('X', buffer.getScreenChar(1, 0))
        assertEquals('b', buffer.getScreenChar(2, 0))
        assertEquals('c', buffer.getScreenChar(3, 0))
        assertEquals(2 to 0, buffer.getCursor())
    }

    @Test
    fun insertText_wraps_overflow_into_next_line() {
        val buffer = TerminalBuffer(width = 5, height = 2, scrollbackMax = 10)
        buffer.setScreenRow(0, "abcdE")
        buffer.setCursor(4, 0)

        buffer.insertText("X")

        assertEquals("abcdX", buffer.getScreenLineAsString(0))
        assertEquals('E', buffer.getScreenChar(0, 1))
        assertEquals(0 to 1, buffer.getCursor())
    }

    @Test
    fun insertText_cascades_across_multiple_lines_without_scroll_when_overflow_empties() {
        val buffer = TerminalBuffer(width = 5, height = 4, scrollbackMax = 10)
        buffer.setScreenRow(0, "12345")
        buffer.setScreenRow(1, "abcde")
        buffer.screen[2][0] = Cell(ch = 'V')
        buffer.screen[2][1] = Cell(ch = 'W')
        buffer.setCursor(0, 0)

        buffer.insertText("Q")

        assertEquals("Q1234", buffer.getScreenLineAsString(0))
        assertEquals("5abcd", buffer.getScreenLineAsString(1))
        assertEquals("eVW", buffer.getScreenLineAsString(2))
        assertEquals("", buffer.getScreenLineAsString(3))
        assertEquals(0, buffer.scrollback.size)
        assertEquals(1 to 0, buffer.getCursor())
    }

    @Test
    fun insertText_cascading_past_last_row_triggers_scrollback() {
        val buffer = TerminalBuffer(width = 5, height = 2, scrollbackMax = 10)
        buffer.setScreenRow(0, "11111")
        buffer.setScreenRow(1, "22222")
        buffer.setCursor(0, 1)

        buffer.insertText("X")

        assertEquals(1, buffer.scrollback.size)
        assertEquals("11111", buffer.getScrollbackLineAsString(0))
        assertEquals("X2222", buffer.getScreenLineAsString(0))
        assertEquals("2", buffer.getScreenLineAsString(1))
        assertEquals(1 to 1, buffer.getCursor())
        assertEquals(buffer.height, buffer.screen.size)
        assertTrue(buffer.screen.all { it.size == buffer.width })
    }

    @Test
    fun insertText_uses_current_attributes_and_preserves_shifted_cell_attributes() {
        val buffer = TerminalBuffer(width = 5, height = 2, scrollbackMax = 10)
        val shiftedOriginal = Cell(
            ch = 'b',
            fg = TermColor.GREEN,
            bg = TermColor.YELLOW,
            style = TextStyle(italic = true)
        )
        buffer.screen[0][0] = Cell(ch = 'a')
        buffer.screen[0][1] = shiftedOriginal
        buffer.screen[0][2] = Cell(ch = 'c')

        buffer.setAttributes(
            fg = TermColor.RED,
            bg = TermColor.BLUE,
            style = TextStyle(bold = true, underline = true)
        )
        buffer.setCursor(1, 0)

        buffer.insertText("X")

        assertEquals(
            Cell(
                ch = 'X',
                fg = TermColor.RED,
                bg = TermColor.BLUE,
                style = TextStyle(bold = true, underline = true)
            ),
            buffer.getScreenCell(1, 0)
        )
        assertEquals(shiftedOriginal, buffer.getScreenCell(2, 0))
    }

    private fun TerminalBuffer.setScreenRow(row: Int, text: String) {
        require(text.length == width) { "text length must equal width" }
        for (col in 0 until width) {
            screen[row][col] = Cell(ch = text[col])
        }
    }

    private fun List<Cell>.toChars(): String = joinToString(separator = "") { it.ch.toString() }

    private fun copyCell(cell: Cell): Cell = cell.copy(style = cell.style.copy())

    private fun deepCopyLines(lines: List<List<Cell>>): List<List<Cell>> =
        lines.map { line -> line.map { copyCell(it) } }
}
