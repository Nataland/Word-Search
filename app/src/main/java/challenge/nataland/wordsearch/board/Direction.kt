package challenge.nataland.wordsearch.board

import challenge.nataland.wordsearch.utils.random

enum class Direction(val increment: Int) {
    LEFT_RIGHT(1),
    RIGHT_LEFT(-1),

    TOP_BOTTOM(10),
    BOTTOM_TOP(-10),

    TOP_LEFT_RIGHT_BOTTOM(11),
    RIGHT_BOTTOM_TOP_LEFT(-11),

    TOP_RIGHT_LEFT_BOTTOM(9),
    LEFT_BOTTOM_TOP_RIGHT(-9);

    fun getCol(wordLength: Int) = when (this) {
        LEFT_RIGHT, TOP_LEFT_RIGHT_BOTTOM, LEFT_BOTTOM_TOP_RIGHT -> (0..10 - wordLength).random()
        RIGHT_LEFT, RIGHT_BOTTOM_TOP_LEFT, TOP_RIGHT_LEFT_BOTTOM -> (wordLength - 1..9).random()
        TOP_BOTTOM, BOTTOM_TOP -> (0..9).random()
    }

    fun getRow(wordLength: Int) = when (this) {
        LEFT_RIGHT, RIGHT_LEFT -> (0..9).random()
        TOP_BOTTOM, TOP_LEFT_RIGHT_BOTTOM, TOP_RIGHT_LEFT_BOTTOM -> (0..10 - wordLength).random()
        BOTTOM_TOP, RIGHT_BOTTOM_TOP_LEFT, LEFT_BOTTOM_TOP_RIGHT -> (wordLength - 1..9).random()
    }
}