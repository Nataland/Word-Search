package challenge.nataland.wordsearch

import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.widget.GridLayout
import android.widget.GridLayout.FILL
import android.widget.GridLayout.UNDEFINED
import android.widget.TextView


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val board = findViewById<GridLayout>(R.id.board)
        val words = listOf("Swift", "Kotlin", "ObjectiveC", "Variable", "Java", "Mobile")
        val boardData = placeWords(words)

        for (cell in boardData) {
            board.addView(TextView(this).apply {
                text = cell.content
                setOnClickListener {
                    setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.lightYellow))
                    if (cell.fullWord == "") {
                        val r = Runnable {
                            setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.colorAccent))
                        }
                        val h = Handler()
                        h.postDelayed(r, 800)
                    }
                }
                setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.colorAccent))
                layoutParams = GridLayout.LayoutParams(
                        GridLayout.spec(UNDEFINED, FILL, 1.0F),
                        GridLayout.spec(UNDEFINED, FILL, 1.0F)
                ).apply {
                    gravity = Gravity.CENTER
                    width = 0
                    height = 0
                }
            })
        }
    }


    private fun placeWords(words: List<String>): List<Cell> {
        val board = MutableList(100) { Cell() }
        var wordsPlaced = 0
        while (wordsPlaced < words.size) {
            val word = words[wordsPlaced]
            val direction = Direction.values()[(0 until Direction.values().size).random()]
            val initialPosition = direction.getRow(word.length) * 10 + direction.getCol(word.length)
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