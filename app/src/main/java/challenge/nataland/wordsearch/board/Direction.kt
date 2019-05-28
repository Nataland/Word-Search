package challenge.nataland.wordsearch.board

import challenge.nataland.wordsearch.game.GRID_SIZE
import challenge.nataland.wordsearch.utils.random

enum class Direction(val increment: Int) {
    LEFT_RIGHT(1),
    RIGHT_LEFT(-1),

    TOP_BOTTOM(GRID_SIZE),
    BOTTOM_TOP(-GRID_SIZE),

    TOP_LEFT_RIGHT_BOTTOM(GRID_SIZE+1),
    RIGHT_BOTTOM_TOP_LEFT(-(GRID_SIZE+1)),

    TOP_RIGHT_LEFT_BOTTOM(GRID_SIZE-1),
    LEFT_BOTTOM_TOP_RIGHT(-(GRID_SIZE-1));

    fun getCol(wordLength: Int) = when (this) {
        LEFT_RIGHT, TOP_LEFT_RIGHT_BOTTOM, LEFT_BOTTOM_TOP_RIGHT -> (0..(GRID_SIZE) - wordLength).random()
        RIGHT_LEFT, RIGHT_BOTTOM_TOP_LEFT, TOP_RIGHT_LEFT_BOTTOM -> (wordLength - 1..(GRID_SIZE-1)).random()
        TOP_BOTTOM, BOTTOM_TOP -> (0..(GRID_SIZE-1)).random()
    }

    fun getRow(wordLength: Int) = when (this) {
        LEFT_RIGHT, RIGHT_LEFT -> (0..(GRID_SIZE-1)).random()
        TOP_BOTTOM, TOP_LEFT_RIGHT_BOTTOM, TOP_RIGHT_LEFT_BOTTOM -> (0..GRID_SIZE - wordLength).random()
        BOTTOM_TOP, RIGHT_BOTTOM_TOP_LEFT, LEFT_BOTTOM_TOP_RIGHT -> (wordLength - 1..(GRID_SIZE-1)).random()
    }
}