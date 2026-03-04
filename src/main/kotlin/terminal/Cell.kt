package terminal

data class Cell(
    val ch: Char = ' ',
    val fg: TermColor = TermColor.DEFAULT,
    val bg: TermColor = TermColor.DEFAULT,
    val style: TextStyle = TextStyle()
)
