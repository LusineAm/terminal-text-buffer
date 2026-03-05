package terminal

data class Cell(
    val ch: Char = ' ',
    val fg: TermColor = TermColor.DEFAULT,
    val bg: TermColor = TermColor.DEFAULT,
    val style: TextStyle = TextStyle()
) {
    val bold: Boolean get() = style.bold
    val italic: Boolean get() = style.italic
    val underline: Boolean get() = style.underline
}
