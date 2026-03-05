package terminal

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ClearOperationsTest {

    @Test
    fun clearScreen_does_not_touch_scrollback_and_blanks_screen() {
        val buffer = TerminalBuffer(width = 5, height = 3, scrollbackMax = 10)
        buffer.setScreenRow(0, "ABCDE")
        buffer.setScreenRow(1, "12345")
        buffer.setScreenRow(2, "xyz!@")
        buffer.scrollback.add(lineOfCells("old01"))
        buffer.scrollback.add(lineOfCells("old02"))
        buffer.setCursor(4, 2)

        val expectedScrollback = buffer.scrollback.map { line -> line.map { it.copy(style = it.style.copy()) } }
        val expectedCursor = buffer.getCursor()

        buffer.clearScreen()

        assertEquals(expectedScrollback, buffer.scrollback)
        assertScreenBlank(buffer)
        assertEquals(expectedCursor, buffer.getCursor())
    }

    @Test
    fun clearScreenAndScrollback_resets_both_screen_and_scrollback_and_cursor() {
        val buffer = TerminalBuffer(width = 5, height = 3, scrollbackMax = 10)
        buffer.setScreenRow(0, "HELLO")
        buffer.setScreenRow(1, "WORLD")
        buffer.setScreenRow(2, "!!!!!")
        buffer.scrollback.add(lineOfCells("hist1"))
        buffer.setCursor(3, 2)

        buffer.clearScreenAndScrollback()

        assertEquals(0, buffer.scrollback.size)
        assertScreenBlank(buffer)
        assertEquals(0 to 0, buffer.getCursor())
    }

    private fun assertScreenBlank(buffer: TerminalBuffer) {
        assertEquals(buffer.height, buffer.screen.size)
        for (row in 0 until buffer.height) {
            assertEquals(buffer.width, buffer.screen[row].size)
            for (col in 0 until buffer.width) {
                assertEquals(Cell(), buffer.screen[row][col], "screen[$row][$col] must be default cell")
            }
        }
    }

    private fun TerminalBuffer.setScreenRow(row: Int, text: String) {
        require(text.length == width) { "text length must equal width" }
        for (col in 0 until width) {
            screen[row][col] = Cell(ch = text[col])
        }
    }

    private fun lineOfCells(text: String): List<Cell> {
        require(text.length == 5) { "test helper expects width 5 text" }
        return text.map { Cell(ch = it) }
    }
}
