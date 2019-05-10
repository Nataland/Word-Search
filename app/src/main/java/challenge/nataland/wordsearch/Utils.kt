package challenge.nataland.wordsearch

import android.content.Context
import android.support.v4.content.ContextCompat
import android.view.View
import java.util.*

fun IntRange.random() =
        Random().nextInt((endInclusive + 1) - start) + start

fun View.setDefaultColor(context: Context) = setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))

fun View.setHighlightColor(context: Context) = setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))

fun View.setCorrectColor(context: Context) = setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent))