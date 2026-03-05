package terminal

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ScrollBehaviorTest {

    @Test
    fun insertEmptyLineAtBottom_pushes_top_line_to_scrollback() {
        val buffer = TerminalBuffer(width = 4, height = 3, scrollbackMax = 10)
        buffer.setScreenRow(0, "AAAA")
        buffer.setScreenRow(1, "BBBB")
        buffer.setScreenRow(2, "CCCC")

        buffer.insertEmptyLineAtBottom()

        assertEquals(1, buffer.scrollback.size)
        assertEquals("AAAA", buffer.scrollback[0].toChars())
        assertEquals("BBBB", buffer.screen[0].toChars())
        assertEquals("CCCC", buffer.screen[1].toChars())
        assertEquals("    ", buffer.screen[2].toChars())
        assertTrue(buffer.screen[2].all { it == Cell() })

        buffer.screen[0][0] = Cell(ch = 'Z')
        assertEquals("AAAA", buffer.scrollback[0].toChars())
    }

    @Test
    fun insertEmptyLineAtBottom_enforces_scrollback_max_and_order() {
        val buffer = TerminalBuffer(width = 2, height = 2, scrollbackMax = 2)
        buffer.setScreenRow(0, "AA")
        buffer.setScreenRow(1, "BB")

        buffer.insertEmptyLineAtBottom()
        buffer.setScreenRow(1, "CC")

        buffer.insertEmptyLineAtBottom()
        buffer.setScreenRow(1, "DD")

        buffer.insertEmptyLineAtBottom()

        assertEquals(2, buffer.scrollback.size)
        assertEquals("BB", buffer.scrollback[0].toChars())
        assertEquals("CC", buffer.scrollback[1].toChars())
    }

    @Test
    fun insertEmptyLineAtBottom_keeps_screen_height_constant() {
        val buffer = TerminalBuffer(width = 3, height = 4, scrollbackMax = 10)

        repeat(12) {
            buffer.insertEmptyLineAtBottom()
            assertEquals(buffer.height, buffer.screen.size)
            assertTrue(buffer.screen.all { it.size == buffer.width })
        }
    }

    private fun TerminalBuffer.setScreenRow(row: Int, text: String) {
        require(text.length == width) { "text length must equal width" }
        for (col in 0 until width) {
            screen[row][col] = Cell(ch = text[col])
        }
    }

    private fun List<Cell>.toChars(): String = joinToString(separator = "") { it.ch.toString() }
}
