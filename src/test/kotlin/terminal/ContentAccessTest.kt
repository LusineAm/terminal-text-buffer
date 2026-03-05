package terminal

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class ContentAccessTest {

    @Test
    fun blank_lines_follow_trim_policy() {
        val buffer = TerminalBuffer(width = 5, height = 3, scrollbackMax = 10)

        assertEquals("", buffer.getLineString(BufferArea.SCREEN, 0))
        assertEquals("\n\n", buffer.getScreenAsString())
        assertEquals("\n\n", buffer.getAllAsString())
    }

    @Test
    fun getAllAsString_orders_scrollback_before_screen() {
        val buffer = TerminalBuffer(width = 4, height = 2, scrollbackMax = 10)
        buffer.scrollback.add(lineOf("AB  "))
        buffer.scrollback.add(lineOf("CDEF"))
        buffer.setScreenRow(0, "12  ")
        buffer.setScreenRow(1, "    ")

        assertEquals("AB\nCDEF\n12\n", buffer.getAllAsString())
    }

    @Test
    fun getChar_and_getAttributes_return_expected_values() {
        val buffer = TerminalBuffer(width = 3, height = 2, scrollbackMax = 10)
        val styled = Cell(
            ch = 'Z',
            fg = TermColor.RED,
            bg = TermColor.BLUE,
            style = TextStyle(bold = true, italic = true)
        )
        buffer.screen[1][2] = styled
        buffer.scrollback.add(
            listOf(
                Cell(ch = 'Q', fg = TermColor.GREEN, bg = TermColor.BLACK, style = TextStyle(underline = true)),
                Cell(),
                Cell()
            )
        )

        assertEquals('Z', buffer.getChar(BufferArea.SCREEN, 1, 2))
        assertEquals(
            Attributes(
                fg = TermColor.RED,
                bg = TermColor.BLUE,
                style = TextStyle(bold = true, italic = true)
            ),
            buffer.getAttributes(BufferArea.SCREEN, 1, 2)
        )
        assertEquals('Q', buffer.getChar(BufferArea.SCROLLBACK, 0, 0))
        assertEquals(
            Attributes(
                fg = TermColor.GREEN,
                bg = TermColor.BLACK,
                style = TextStyle(underline = true)
            ),
            buffer.getAttributes(BufferArea.SCROLLBACK, 0, 0)
        )
        assertEquals(' ', buffer.getChar(BufferArea.SCREEN, 0, 0))
        assertEquals(Attributes(), buffer.getAttributes(BufferArea.SCREEN, 0, 0))
    }

    @Test
    fun getters_use_strict_bounds() {
        val buffer = TerminalBuffer(width = 3, height = 2, scrollbackMax = 10)

        assertThrows(IndexOutOfBoundsException::class.java) {
            buffer.getCell(BufferArea.SCREEN, row = 2, col = 0)
        }
        assertThrows(IndexOutOfBoundsException::class.java) {
            buffer.getCell(BufferArea.SCREEN, row = 0, col = 3)
        }
        assertThrows(IndexOutOfBoundsException::class.java) {
            buffer.getCell(BufferArea.SCROLLBACK, row = 0, col = 0)
        }
    }

    private fun TerminalBuffer.setScreenRow(row: Int, text: String) {
        require(text.length == width) { "text length must equal width" }
        for (col in 0 until width) {
            screen[row][col] = Cell(ch = text[col])
        }
    }

    private fun lineOf(text: String): List<Cell> = text.map { Cell(ch = it) }
}
