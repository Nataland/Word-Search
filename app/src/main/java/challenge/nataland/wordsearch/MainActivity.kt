package challenge.nataland.wordsearch

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.GridLayout
import android.widget.GridLayout.FILL
import android.widget.GridLayout.UNDEFINED
import android.widget.TextView
import com.groupon.grox.Store
import com.groupon.grox.rxjava2.RxStores.states
import com.jakewharton.rxbinding2.view.RxView.clicks
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.disposables.CompositeDisposable


class MainActivity : AppCompatActivity() {

    private val boardState = Store(BoardState())
    private val compositeDisposable = CompositeDisposable()
    private val words = listOf("Swift", "Kotlin", "ObjectiveC", "Variable", "Java", "Mobile").sortedByDescending { it.length }

    private lateinit var board: GridLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        board = findViewById(R.id.board)
        val boardData = placeWords(words)

        compositeDisposable.add(states(boardState).observeOn(mainThread()).subscribe(this::updateUI, this::doLog))

        for ((index, cell) in boardData.withIndex()) {
            board.addView(TextView(this).apply {
                //todo: setTextSize
                text = cell.content
                compositeDisposable.add(
                        clicks(this).subscribe {
                            boardState.dispatch {
                                oldState -> oldState.copy(
                                    currentCell = cell,
                                    currentCellPos = index
                                )
                            }
                        })
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

    private fun updateUI(state: BoardState) {
        if (state.currentCellPos != 0 && state.currentCell.fullWord.isNotEmpty()) {
            setClickColour(board.getChildAt(state.currentCellPos))
        }
    }

    private fun doLog(throwable: Throwable) {
        Log.d("Grox", "An error occurred in a Grox chain.", throwable)
    }

    data class BoardState(
            val currentCell: Cell = Cell(),
            val currentCellPos: Int = 0,
            val currentWord: String = "",
            val currentWordCorrectCharPositions: List<Int> = emptyList(),
            val wordsFound: List<String> = emptyList()
    )

    private fun setClickColour(view: View) = view.setBackgroundColor(ContextCompat.getColor(this, R.color.lightYellow))

    private fun setCorrectColour(view: View) = view.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))

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
