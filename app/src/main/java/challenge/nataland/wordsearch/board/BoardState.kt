package challenge.nataland.wordsearch.board

data class BoardState(
        val lastCellPos: Int = -1,
        val currentCellPos: Int = -1,
        val currentWord: String = "",
        val currentWordCorrectCharPositions: List<Int> = emptyList(),
        val wordsFound: List<String> = emptyList(),
        val startTime: Long = 0L,
        val finishTime: Long = 0L,
        val allCorrectCharPositions: List<Int> = emptyList(),
        val board: List<Cell> = emptyList()
)