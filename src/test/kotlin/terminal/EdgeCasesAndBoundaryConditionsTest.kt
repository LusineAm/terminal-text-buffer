package terminal

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class EdgeCasesAndBoundaryConditionsTest {

    @Test
    fun scrollback_disabled_discards_scrolled_off_lines_and_keeps_visible_content_correct() {
        val buffer = TerminalBuffer(width = 3, height = 2, scrollbackMax = 0)

        buffer.writeText("abcdefghi")

        assertEquals(0, buffer.scrollback.size)
        assertRowChars(buffer, row = 0, expected = "ghi")
        assertRowChars(buffer, row = 1, expected = "   ")
        assertEquals(0 to 1, buffer.getCursor())
    }

    @Test
    fun clearScreenAndScrollback_on_scrollback_disabled_buffer_resets_screen_and_cursor() {
        val buffer = TerminalBuffer(width = 3, height = 2, scrollbackMax = 0)

        buffer.writeText("abcdefghi")
        buffer.clearScreenAndScrollback()

        assertEquals(0, buffer.scrollback.size)
        assertRowChars(buffer, row = 0, expected = "   ")
        assertRowChars(buffer, row = 1, expected = "   ")
        assertEquals(0 to 0, buffer.getCursor())
    }

    @Test
    fun tiny_1x1_buffer_writeText_behaves_consistently() {
        val buffer = TerminalBuffer(width = 1, height = 1, scrollbackMax = 10)

        buffer.writeText("ABC")

        assertEquals("A", buffer.getLineString(BufferArea.SCROLLBACK, 0))
        assertEquals("B", buffer.getLineString(BufferArea.SCROLLBACK, 1))
        assertEquals("C", buffer.getLineString(BufferArea.SCROLLBACK, 2))
        assertEquals("", buffer.getLineString(BufferArea.SCREEN, 0))
        assertEquals(3, buffer.scrollback.size)
        assertEquals(0 to 0, buffer.getCursor())
    }

    @Test
    fun tiny_1x1_buffer_insert_fill_clear_and_cursor_clamp_behave_consistently() {
        val buffer = TerminalBuffer(width = 1, height = 1, scrollbackMax = 2)

        buffer.insertText("XY")
        assertEquals(2, buffer.scrollback.size)
        assertEquals("X", buffer.getLineString(BufferArea.SCROLLBACK, 0))
        assertEquals("Y", buffer.getLineString(BufferArea.SCROLLBACK, 1))
        assertEquals("", buffer.getScreenAsString())

        buffer.fillCurrentLine('Z')
        assertEquals('Z', buffer.getChar(BufferArea.SCREEN, row = 0, col = 0))
        assertEquals("Z", buffer.getScreenAsString())
        assertEquals("X\nY\nZ", buffer.getAllAsString())

        buffer.setCursor(col = 999, row = -999)
        assertEquals(0 to 0, buffer.getCursor())

        buffer.clearScreen()
        assertEquals("", buffer.getScreenAsString())
        assertEquals("X\nY\n", buffer.getAllAsString())

        buffer.clearScreenAndScrollback()
        assertEquals("", buffer.getAllAsString())
        assertEquals(0 to 0, buffer.getCursor())
    }

    @Test
    fun setCursor_clamps_negative_and_too_large_coordinates_without_mutating_content() {
        val buffer = TerminalBuffer(width = 4, height = 3, scrollbackMax = 5)

        buffer.writeText("ABCDEFGHIJKL")

        val screenBefore = buffer.getScreenAsString()
        val allBefore = buffer.getAllAsString()

        buffer.setCursor(-10, 1)
        assertEquals(0 to 1, buffer.getCursor())

        buffer.setCursor(2, -10)
        assertEquals(2 to 0, buffer.getCursor())

        buffer.setCursor(999, 1)
        assertEquals(3 to 1, buffer.getCursor())

        buffer.setCursor(2, 999)
        assertEquals(2 to 2, buffer.getCursor())

        buffer.setCursor(-5, 999)
        assertEquals(0 to 2, buffer.getCursor())

        buffer.setCursor(999, -5)
        assertEquals(3 to 0, buffer.getCursor())

        assertEquals(screenBefore, buffer.getScreenAsString())
        assertEquals(allBefore, buffer.getAllAsString())
    }

    @Test
    fun insertText_very_long_string_cascades_over_multiple_lines_and_scrolls_past_bottom() {
        val buffer = TerminalBuffer(width = 4, height = 2, scrollbackMax = 10)

        setScreenRow(buffer, row = 0, text = "ABCD")
        setScreenRow(buffer, row = 1, text = "EFGH")
        buffer.setCursor(2, 0)

        buffer.insertText("123456789")

        assertEquals("AB1C", buffer.getLineString(BufferArea.SCROLLBACK, 0))
        assertEquals("DEF2", buffer.getLineString(BufferArea.SCROLLBACK, 1))
        assertEquals("345G", buffer.getLineString(BufferArea.SCROLLBACK, 2))

        assertRowChars(buffer, row = 0, expected = "H  6")
        assertRowChars(buffer, row = 1, expected = "789 ")
        assertEquals(3, buffer.scrollback.size)
        assertEquals(3 to 1, buffer.getCursor())
    }

    @Test
    fun writeText_exact_screen_capacity_has_expected_boundary_behavior() {
        val buffer = TerminalBuffer(width = 4, height = 2, scrollbackMax = 10)

        buffer.writeText("abcdefgh")

        assertEquals(1, buffer.scrollback.size)
        assertEquals("abcd", buffer.getLineString(BufferArea.SCROLLBACK, 0))
        assertRowChars(buffer, row = 0, expected = "efgh")
        assertRowChars(buffer, row = 1, expected = "    ")
        assertEquals(0 to 1, buffer.getCursor())
    }

    @Test
    fun writeText_screen_capacity_plus_one_has_expected_boundary_behavior() {
        val buffer = TerminalBuffer(width = 4, height = 2, scrollbackMax = 10)

        buffer.writeText("abcdefghi")

        assertEquals(1, buffer.scrollback.size)
        assertEquals("abcd", buffer.getLineString(BufferArea.SCROLLBACK, 0))
        assertRowChars(buffer, row = 0, expected = "efgh")
        assertRowChars(buffer, row = 1, expected = "i   ")
        assertEquals(1 to 1, buffer.getCursor())
    }

    @Test
    fun setAttributes_then_write_then_change_attributes_preserves_cell_attribute_snapshots() {
        val buffer = TerminalBuffer(width = 6, height = 1, scrollbackMax = 10)

        val firstStyle = TextStyle(bold = true)
        buffer.setAttributes(TermColor.RED, TermColor.BLUE, firstStyle)
        buffer.writeText("ab")

        val secondStyle = TextStyle(italic = true, underline = true)
        buffer.setAttributes(TermColor.GREEN, TermColor.BLACK, secondStyle)
        buffer.writeText("cd")

        buffer.setAttributes(TermColor.BRIGHT_WHITE, TermColor.MAGENTA, TextStyle())

        assertEquals('a', buffer.getChar(BufferArea.SCREEN, row = 0, col = 0))
        assertEquals('b', buffer.getChar(BufferArea.SCREEN, row = 0, col = 1))
        assertEquals('c', buffer.getChar(BufferArea.SCREEN, row = 0, col = 2))
        assertEquals('d', buffer.getChar(BufferArea.SCREEN, row = 0, col = 3))

        assertEquals(
            Attributes(fg = TermColor.RED, bg = TermColor.BLUE, style = firstStyle),
            buffer.getAttributes(BufferArea.SCREEN, row = 0, col = 0)
        )
        assertEquals(
            Attributes(fg = TermColor.RED, bg = TermColor.BLUE, style = firstStyle),
            buffer.getAttributes(BufferArea.SCREEN, row = 0, col = 1)
        )
        assertEquals(
            Attributes(fg = TermColor.GREEN, bg = TermColor.BLACK, style = secondStyle),
            buffer.getAttributes(BufferArea.SCREEN, row = 0, col = 2)
        )
        assertEquals(
            Attributes(fg = TermColor.GREEN, bg = TermColor.BLACK, style = secondStyle),
            buffer.getAttributes(BufferArea.SCREEN, row = 0, col = 3)
        )
    }

    private fun setScreenRow(buffer: TerminalBuffer, row: Int, text: String) {
        require(text.length == buffer.width) { "text length must equal width" }
        for (col in 0 until buffer.width) {
            buffer.screen[row][col] = Cell(ch = text[col])
        }
    }

    private fun assertRowChars(buffer: TerminalBuffer, row: Int, expected: String) {
        require(expected.length == buffer.width) { "expected length must equal width" }
        for (col in 0 until buffer.width) {
            assertEquals(expected[col], buffer.getChar(BufferArea.SCREEN, row, col))
        }
    }
}
