package challenge.nataland.wordsearch

import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.TypedValue
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
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val boardState = Store(BoardState())
    private val compositeDisposable = CompositeDisposable()
    private val words = listOf("Swift", "Kotlin", "ObjectiveC", "Variable", "Java", "Mobile")
    private val letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        hideSuccessMessage()
        compositeDisposable.add(states(boardState).observeOn(mainThread()).subscribe(this::updateUI, this::doLog))
        setupBoard()
    }

    private fun setupBoard() {
        checklist.removeAllViews()
        for (word in words) {
            checklist.addView(
                    TextView(this).apply {
                        text = word
                        compoundDrawablePadding = resources.getDimension(R.dimen.drawable_padding).toInt()
                        setCompoundDrawablesWithIntrinsicBounds(getDrawable(R.drawable.plain_box), null, null, null)
                        setTextColor(ContextCompat.getColor(this@MainActivity, R.color.white))
                        layoutParams = GridLayout.LayoutParams(
                                GridLayout.spec(UNDEFINED, FILL, 1.0F),
                                GridLayout.spec(UNDEFINED, FILL, 1.0F)
                        ).apply {
                            gravity = Gravity.CENTER_VERTICAL
                            width = 0
                            height = 0
                        }
                    }
            )
        }

        board.removeAllViews()
        val boardData = placeWords(words)
        for ((index, cell) in boardData.withIndex()) {
            board.addView(TextView(this).apply {
                text = if (cell.content.isEmpty()) letters[(0 until letters.length).random()].toString() else cell.content
                typeface = ResourcesCompat.getFont(this@MainActivity, R.font.gothamssm_medium)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 20F)
                setTextColor(ContextCompat.getColor(this@MainActivity, R.color.white))
                setDefaultColor(this@MainActivity)
                compositeDisposable.add(
                        clicks(this).subscribe {
                            boardState.dispatch { oldState ->
                                oldState.copy(
                                        currentCell = cell,
                                        currentCellPos = index
                                )
                            }
                        })
                compositeDisposable.add(
                        clicks(play_again_button).subscribe {
                            hideSuccessMessage()
                            boardState.dispatch { BoardState() }
                            setupBoard()
                        }
                )
                layoutParams = GridLayout.LayoutParams(
                        GridLayout.spec(UNDEFINED, FILL, 1.0F),
                        GridLayout.spec(UNDEFINED, FILL, 1.0F)
                ).apply {
                    setMargins(1, 1, 1, 1)
                    gravity = Gravity.CENTER
                    width = 0
                    height = 0
                }
            })
        }
    }

    private fun updateUI(state: BoardState) {
        if (state.wordsFound.size == words.size) {
            showSuccessMessage(System.currentTimeMillis() - state.startTime)
            return
        }

        if (state.startTime == 0L) {
            boardState.dispatch { oldState -> oldState.copy(startTime = System.currentTimeMillis()) }
        }

        if (state.currentCellPos != -1 && state.currentCell.fullWord == "") {
            board.getChildAt(state.currentCellPos).setHighlightColor(this)
            setCellsBackToDefault(state)
        }

        if (state.currentCellPos != -1
                && state.currentCell.fullWord.isNotEmpty()
                && !state.currentWordCorrectCharPositions.contains(state.currentCellPos)
                && !state.wordsFound.contains(state.currentWord)
                && !state.wordsFound.contains(state.currentCell.fullWord)) {

            board.getChildAt(state.currentCellPos).setHighlightColor(this)

            when {
                state.currentWord.isEmpty() -> boardState.dispatch { oldState ->
                    oldState.copy(
                            currentCell = Cell(),
                            currentCellPos = -1,
                            currentWordCorrectCharPositions = oldState.currentWordCorrectCharPositions + oldState.currentCellPos,
                            currentWord = oldState.currentCell.fullWord
                    )
                }
                state.currentWord == state.currentCell.fullWord -> boardState.dispatch { oldState ->
                    oldState.copy(
                            currentCell = Cell(),
                            currentCellPos = -1,
                            currentWordCorrectCharPositions = oldState.currentWordCorrectCharPositions + oldState.currentCellPos
                    )
                }
                else -> {
                    setCellsBackToDefault(state)
                }
            }
        }

        if (state.currentWord.isNotEmpty() && state.currentWord.length == state.currentWordCorrectCharPositions.size) {
            (checklist.getChildAt(words.indexOf(state.currentWord)) as TextView).setCompoundDrawablesWithIntrinsicBounds(
                    getDrawable(R.drawable.checked_box), null, null, null
            )

            state.currentWordCorrectCharPositions.forEach {
                board.getChildAt(it).setCorrectColor(this)
            }

            boardState.dispatch { oldState ->
                oldState.copy(
                        currentCell = Cell(),
                        currentCellPos = -1,
                        currentWord = "",
                        currentWordCorrectCharPositions = emptyList(),
                        wordsFound = oldState.wordsFound + oldState.currentWord
                )
            }
        }
    }

    private fun setCellsBackToDefault(state: BoardState) {
        val r = Runnable {
            (state.currentWordCorrectCharPositions + state.currentCellPos).forEach {
                board.getChildAt(it).setDefaultColor(this)
            }
            boardState.dispatch { oldState ->
                oldState.copy(
                        currentCell = Cell(),
                        currentCellPos = -1,
                        currentWord = "",
                        currentWordCorrectCharPositions = emptyList()
                )
            }
        }
        val h = Handler()
        h.postDelayed(r, 100)
    }

    private fun showSuccessMessage(time: Long) {
        congrats_message.text = String.format(resources.getString(R.string.game_complete_text), (time.toFloat() / 1000F))
        congrats_message.visibility = View.VISIBLE
        play_again_button.visibility = View.VISIBLE
        checklist.visibility = View.GONE
    }

    private fun hideSuccessMessage() {
        congrats_message.visibility = View.GONE
        play_again_button.visibility = View.GONE
        checklist.visibility = View.VISIBLE
    }

    private fun doLog(throwable: Throwable) {
        Log.d("Grox", "An error occurred in a Grox chain.", throwable)
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

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }
}
