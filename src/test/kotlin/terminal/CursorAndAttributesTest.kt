package terminal

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CursorAndAttributesTest {

    @Test
    fun setCursor_clamps_to_bounds() {
        val buffer = TerminalBuffer(width = 5, height = 3, scrollbackMax = 10)

        buffer.setCursor(0, 0)
        assertEquals(0 to 0, buffer.getCursor())

        buffer.setCursor(4, 2)
        assertEquals(4 to 2, buffer.getCursor())

        buffer.setCursor(-1, -1)
        assertEquals(0 to 0, buffer.getCursor())

        buffer.setCursor(99, 99)
        assertEquals(4 to 2, buffer.getCursor())
    }

    @Test
    fun move_operations_clamp_to_bounds() {
        val buffer = TerminalBuffer(width = 5, height = 3, scrollbackMax = 10)

        buffer.moveLeft(1)
        assertEquals(0 to 0, buffer.getCursor())

        buffer.moveUp(10)
        assertEquals(0 to 0, buffer.getCursor())

        buffer.setCursor(4, 2)
        buffer.moveRight(1)
        assertEquals(4 to 2, buffer.getCursor())

        buffer.moveDown(1)
        assertEquals(4 to 2, buffer.getCursor())

        buffer.setCursor(2, 1)
        buffer.moveLeft(10)
        assertEquals(0 to 1, buffer.getCursor())

        buffer.setCursor(2, 1)
        buffer.moveRight(10)
        assertEquals(4 to 1, buffer.getCursor())

        buffer.setCursor(2, 1)
        buffer.moveUp(10)
        assertEquals(2 to 0, buffer.getCursor())

        buffer.setCursor(2, 1)
        buffer.moveDown(10)
        assertEquals(2 to 2, buffer.getCursor())
    }

    @Test
    fun move_with_non_positive_n_does_nothing() {
        val buffer = TerminalBuffer(width = 5, height = 3, scrollbackMax = 10)
        buffer.setCursor(2, 1)

        buffer.moveRight(0)
        assertEquals(2 to 1, buffer.getCursor())

        buffer.moveDown(-3)
        assertEquals(2 to 1, buffer.getCursor())
    }

    @Test
    fun setAttributes_updates_current_state() {
        val buffer = TerminalBuffer(width = 5, height = 3, scrollbackMax = 10)

        assertEquals(Attributes(), buffer.getCurrentAttributes())

        val bold = TextStyle(bold = true)
        buffer.setAttributes(TermColor.RED, TermColor.BLUE, bold)
        assertEquals(
            Attributes(
                fg = TermColor.RED,
                bg = TermColor.BLUE,
                style = TextStyle(bold = true)
            ),
            buffer.getCurrentAttributes()
        )

        val snapshot = buffer.getCurrentAttributes()
        buffer.setAttributes(TermColor.DEFAULT, TermColor.DEFAULT, TextStyle())

        assertEquals(Attributes(), buffer.getCurrentAttributes())
        assertEquals(
            Attributes(
                fg = TermColor.RED,
                bg = TermColor.BLUE,
                style = TextStyle(bold = true)
            ),
            snapshot
        )
    }
}
