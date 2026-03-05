package terminal

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TerminalBufferInitTest {

    @Test
    fun init_creates_blank_screen_with_cursor_at_origin() {
        val width = 5
        val height = 3
        val buffer = TerminalBuffer(width, height, scrollbackMax = 10)

        assertEquals(height, buffer.screen.size)
        buffer.screen.forEach { line ->
            assertEquals(width, line.size)
            line.forEach { cell ->
                assertEquals(' ', cell.ch)
                assertEquals(TermColor.DEFAULT, cell.fg)
                assertEquals(TermColor.DEFAULT, cell.bg)
                assertFalse(cell.bold)
            }
        }

        assertTrue(buffer.scrollback.isEmpty())
        assertEquals(0, buffer.cursorCol)
        assertEquals(0, buffer.cursorRow)

        assertEquals(TermColor.DEFAULT, buffer.currentFg)
        assertEquals(TermColor.DEFAULT, buffer.currentBg)
        assertFalse(buffer.currentBold)
    }

    @Test
    fun init_rejects_invalid_sizes() {
        assertThrows<IllegalArgumentException> { TerminalBuffer(0, 1, 0) }
        assertThrows<IllegalArgumentException> { TerminalBuffer(-1, 1, 0) }

        assertThrows<IllegalArgumentException> { TerminalBuffer(1, 0, 0) }
        assertThrows<IllegalArgumentException> { TerminalBuffer(1, -1, 0) }

        assertThrows<IllegalArgumentException> { TerminalBuffer(1, 1, -1) }
    }
}
