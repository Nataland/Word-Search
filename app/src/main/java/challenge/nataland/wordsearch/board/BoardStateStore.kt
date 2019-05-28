package challenge.nataland.wordsearch.board

import challenge.nataland.wordsearch.game.GRID_SIZE
import challenge.nataland.wordsearch.game.words
import challenge.nataland.wordsearch.utils.random
import com.groupon.grox.Store

class BoardStateStore {

    val store = Store(BoardState())

    fun startTimer() = store.dispatch { oldState ->
        oldState.copy(finishTime = System.currentTimeMillis())
    }


    fun initializeBoard() = store.dispatch { oldState ->
        oldState.copy(
                board = placeWords(words),
                startTime = System.currentTimeMillis()
        )
    }


    fun setCurrentCellToNull(lastCellPos: Int) = store.dispatch { oldState ->
        oldState.copy(
                lastCellPos = lastCellPos,
                currentCellPos = -1,
                currentWord = "",
                currentWordCorrectCharPositions = emptyList()
        )
    }


    fun setCurrentCellPosition(currentCellPos: Int) = store.dispatch { oldState ->
        oldState.copy(currentCellPos = currentCellPos)
    }


    fun foundAWord(state: BoardState) = store.dispatch { oldState ->
        oldState.copy(
                currentCellPos = -1,
                lastCellPos = state.currentCellPos,
                currentWord = "",
                allCorrectCharPositions = state.allCorrectCharPositions + state.currentWordCorrectCharPositions,
                currentWordCorrectCharPositions = emptyList(),
                wordsFound = state.wordsFound + state.currentWord
        )
    }


    fun foundFirstCharOfAWord(state: BoardState) = store.dispatch { oldState ->
        oldState.copy(
                currentWordCorrectCharPositions = state.currentWordCorrectCharPositions + state.currentCellPos,
                currentWord = state.board[state.currentCellPos].fullWord,
                currentCellPos = -1,
                lastCellPos = state.currentCellPos
        )
    }


    fun foundAnotherChar(state: BoardState) = store.dispatch { oldState ->
        oldState.copy(
                currentCellPos = -1,
                lastCellPos = state.currentCellPos,
                currentWordCorrectCharPositions = state.currentWordCorrectCharPositions + state.currentCellPos
        )
    }

    fun restart() = store.dispatch { BoardState() }

    private fun placeWords(words: List<String>): List<Cell> {
        val board = MutableList(GRID_SIZE * GRID_SIZE) { Cell() }
        var wordsPlaced = 0
        while (wordsPlaced < words.size) {
            val word = words[wordsPlaced]
            val direction = Direction.values()[(0 until Direction.values().size).random()]
            val initialPosition = direction.getRow(word.length) * GRID_SIZE + direction.getCol(word.length)
            var placedAWord = true
            var charsPlaced = 0
            val placedLocations = mutableListOf<Int>()
            while (charsPlaced < word.length) {
                val char = word[charsPlaced]
                val currentPosition = initialPosition + charsPlaced * direction.increment
                if (board[currentPosition].content == "") {
                    board[currentPosition].content = char.toString()
                    board[currentPosition].fullWord = word
                    placedLocations.add(currentPosition)
                    charsPlaced++
                } else {
                    placedAWord = false
                    placedLocations.forEach { board[it].empty() }
                    break
                }
            }
            if (placedAWord) wordsPlaced++
        }
        return board
    }
}