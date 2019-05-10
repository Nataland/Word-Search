package challenge.nataland.wordsearch

data class BoardState(
        val currentCell: Cell = Cell(),
        val currentCellPos: Int = 0,
        val currentWord: String = "",
        val currentWordCorrectCharPositions: List<Int> = emptyList(),
        val wordsFound: List<String> = emptyList()
)