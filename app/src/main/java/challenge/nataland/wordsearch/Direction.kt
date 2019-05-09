package challenge.nataland.wordsearch

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
        Direction.LEFT_RIGHT, Direction.TOP_LEFT_RIGHT_BOTTOM, Direction.LEFT_BOTTOM_TOP_RIGHT -> (0..10 - wordLength).random()
        Direction.RIGHT_LEFT, Direction.RIGHT_BOTTOM_TOP_LEFT, Direction.TOP_RIGHT_LEFT_BOTTOM -> (wordLength - 1..9).random()
        Direction.TOP_BOTTOM, Direction.BOTTOM_TOP -> (0..9).random()
    }

    fun getRow(wordLength: Int) = when (this) {
        Direction.LEFT_RIGHT, Direction.RIGHT_LEFT -> (0..9).random()
        Direction.TOP_BOTTOM, Direction.TOP_LEFT_RIGHT_BOTTOM, Direction.TOP_RIGHT_LEFT_BOTTOM -> (0..10 - wordLength).random()
        Direction.BOTTOM_TOP, Direction.RIGHT_BOTTOM_TOP_LEFT, Direction.LEFT_BOTTOM_TOP_RIGHT -> (wordLength - 1..9).random()
    }
}