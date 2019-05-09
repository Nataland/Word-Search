package challenge.nataland.wordsearch

import java.util.*

fun IntRange.random() =
        Random().nextInt((endInclusive + 1) - start) + start