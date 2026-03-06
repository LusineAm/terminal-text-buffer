package terminal

data class Attributes(
    val fg: TermColor = TermColor.DEFAULT,
    val bg: TermColor = TermColor.DEFAULT,
    val style: TextStyle = TextStyle()
)
