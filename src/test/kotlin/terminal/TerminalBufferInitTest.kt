package terminal

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TerminalBufferInitTest {

    @Test
    fun constructor_initializes_blank_screen_and_empty_scrollback() {
        val buffer = TerminalBuffer(width = 5, height = 3, scrollbackMax = 10)

        assertEquals(3, buffer.screen.size)
        assertTrue(buffer.screen.all { line -> line.size == 5 })
        assertTrue(buffer.screen.all { line -> line.all { cell -> cell == Cell() } })
        assertTrue(buffer.scrollback.isEmpty())
    }

    @Test
    fun constructor_sets_default_cursor_and_attributes() {
        val buffer = TerminalBuffer(width = 5, height = 3, scrollbackMax = 10)

        assertEquals(0 to 0, buffer.getCursor())
        assertEquals(Attributes(), buffer.getCurrentAttributes())
    }

    @Test
    fun constructor_rejects_non_positive_width() {
        val zeroWidth = assertThrows(IllegalArgumentException::class.java) {
            TerminalBuffer(width = 0, height = 3, scrollbackMax = 10)
        }
        assertEquals("width must be > 0 (was 0)", zeroWidth.message)

        val negativeWidth = assertThrows(IllegalArgumentException::class.java) {
            TerminalBuffer(width = -1, height = 3, scrollbackMax = 10)
        }
        assertEquals("width must be > 0 (was -1)", negativeWidth.message)
    }

    @Test
    fun constructor_rejects_non_positive_height() {
        val zeroHeight = assertThrows(IllegalArgumentException::class.java) {
            TerminalBuffer(width = 5, height = 0, scrollbackMax = 10)
        }
        assertEquals("height must be > 0 (was 0)", zeroHeight.message)

        val negativeHeight = assertThrows(IllegalArgumentException::class.java) {
            TerminalBuffer(width = 5, height = -1, scrollbackMax = 10)
        }
        assertEquals("height must be > 0 (was -1)", negativeHeight.message)
    }

    @Test
    fun constructor_rejects_negative_scrollback_max() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            TerminalBuffer(width = 5, height = 3, scrollbackMax = -1)
        }
        assertEquals("scrollbackMax must be >= 0 (was -1)", exception.message)
    }
}
