package challenge.nataland.wordsearch.game

import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.GridLayout
import android.widget.GridLayout.FILL
import android.widget.GridLayout.UNDEFINED
import android.widget.TextView
import challenge.nataland.wordsearch.*
import challenge.nataland.wordsearch.app.MyApplication
import challenge.nataland.wordsearch.board.BoardState
import challenge.nataland.wordsearch.board.Cell
import challenge.nataland.wordsearch.board.Direction
import challenge.nataland.wordsearch.utils.random
import challenge.nataland.wordsearch.utils.setCorrectColor
import challenge.nataland.wordsearch.utils.setDefaultColor
import challenge.nataland.wordsearch.utils.setHighlightColor
import com.groupon.grox.Store
import com.groupon.grox.rxjava2.RxStores.states
import com.jakewharton.rxbinding2.view.RxView.clicks
import com.jakewharton.rxbinding2.view.RxView.touches
import dagger.Component
import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject
import javax.inject.Singleton


class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var myExample: Store<BoardState>

    private val compositeDisposable = CompositeDisposable()

    private val words = listOf("Swift", "Kotlin", "ObjectiveC", "Variable", "Java", "Mobile")
    private val letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        (application as MyApplication)
                .myComponent
                .inject(this@MainActivity)

        compositeDisposable.add(states(myExample).observeOn(mainThread()).subscribe(this::updateUI, this::doLog))
    }

    private fun setupChecklist(state: BoardState) {
        if (checklist.childCount != words.size) {
            for (word in words) {
                checklist.addView(
                        TextView(this).apply {
                            text = word
                            compoundDrawablePadding = resources.getDimension(R.dimen.drawable_padding).toInt()

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
        }

        (0 until checklist.childCount)
                .map { checklist.getChildAt(it) as TextView }
                .forEach {
                    it.setCompoundDrawablesWithIntrinsicBounds(
                            getDrawable(if (state.wordsFound.contains(it.text)) R.drawable.checked_box else R.drawable.plain_box),
                            null,
                            null,
                            null
                    )
                }
    }

    private fun setupBoard(state: BoardState) {
        if (board.childCount != 100) {
            for (cell in state.board) {
                board.addView(TextView(this).apply {
                    text = if (cell.content.isEmpty()) letters[(0 until letters.length).random()].toString() else cell.content
                    typeface = ResourcesCompat.getFont(this@MainActivity, R.font.gothamssm_medium)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 20F)
                    setTextColor(ContextCompat.getColor(this@MainActivity, R.color.white))

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

            compositeDisposable.add(
                    clicks(play_again_button).subscribe {
                        hideSuccessMessage()
                        myExample.dispatch { BoardState() }
                    }
            )

            compositeDisposable.add(touches(board)
                    .map { event -> getCell(event) }
                    .distinctUntilChanged()
                    .subscribe { myExample.dispatch {
                        oldState -> oldState.copy(currentCellPos = it)
                    } }
            )
        }

        for (index in 0 until board.childCount) {
            val child = board.getChildAt(index)
            when {
                state.allCorrectCharPositions.contains(index) -> child.setCorrectColor(this@MainActivity)
                state.currentWordCorrectCharPositions.contains(index) -> child.setHighlightColor(this@MainActivity)
                else -> child.setDefaultColor(this@MainActivity)
            }
        }
    }

    private fun setupSuccessMessage(state: BoardState) {
        if (state.finishTime != 0L) {
            showSuccessMessage(state.finishTime - state.startTime)
        } else {
            hideSuccessMessage()
        }
    }

    private fun updateUI(state: BoardState) {
        if (state.startTime == 0L) {
            board.removeAllViews()
            checklist.removeAllViews()

            myExample.dispatch { oldState ->
                oldState.copy(
                        board = placeWords(words),
                        startTime = System.currentTimeMillis()
                )
            }
        }

        setupBoard(state)
        setupChecklist(state)
        setupSuccessMessage(state)

        if (state.finishTime == 0L && state.wordsFound.size == words.size) {
            myExample.dispatch { oldState -> oldState.copy(finishTime = System.currentTimeMillis()) }
        }

        if (state.currentCellPos != -1 && state.board[state.currentCellPos].fullWord == "") {
            board.getChildAt(state.currentCellPos).setHighlightColor(this)
            setCellsBackToDefault(state)
        }

        if (state.currentCellPos != -1
                && state.board[state.currentCellPos].fullWord.isNotEmpty()
                && !state.currentWordCorrectCharPositions.contains(state.currentCellPos)
                && !state.wordsFound.contains(state.currentWord)
                && !state.wordsFound.contains(state.board[state.currentCellPos].fullWord)) {

            board.getChildAt(state.currentCellPos).setHighlightColor(this)

            when {
                state.currentWord.isEmpty() -> myExample.dispatch { oldState ->
                    oldState.copy(
                            currentWordCorrectCharPositions = state.currentWordCorrectCharPositions + state.currentCellPos,
                            currentWord = state.board[state.currentCellPos].fullWord,
                            currentCellPos = -1,
                            lastCellPos = state.currentCellPos
                    )
                }
                state.currentWord == state.board[state.currentCellPos].fullWord -> myExample.dispatch { oldState ->
                    oldState.copy(
                            currentCellPos = -1,
                            lastCellPos = state.currentCellPos,
                            currentWordCorrectCharPositions = state.currentWordCorrectCharPositions + state.currentCellPos
                    )
                }
                else -> {
                    setCellsBackToDefaultFromCorrectWord(state)
                }
            }
        }

        if (state.currentWord.isNotEmpty() && state.currentWord.length == state.currentWordCorrectCharPositions.size) {
            myExample.dispatch { oldState ->
                oldState.copy(
                        currentCellPos = -1,
                        lastCellPos = state.currentCellPos,
                        currentWord = "",
                        allCorrectCharPositions = state.allCorrectCharPositions + state.currentWordCorrectCharPositions,
                        currentWordCorrectCharPositions = emptyList(),
                        wordsFound = state.wordsFound + state.currentWord
                )
            }
        }
    }

    private fun setCellsBackToDefault(state: BoardState) {
        val r = Runnable {
            (state.currentWordCorrectCharPositions + state.currentCellPos).forEach {
                board.getChildAt(it).setDefaultColor(this)
            }
            myExample.dispatch { oldState ->
                oldState.copy(
                        lastCellPos = state.currentCellPos,
                        currentCellPos = -1,
                        currentWord = "",
                        currentWordCorrectCharPositions = emptyList()
                )
            }
        }
        val h = Handler()
        h.postDelayed(r, 100)
    }

    private fun setCellsBackToDefaultFromCorrectWord(state: BoardState) {
        val r = Runnable {
            (state.currentWordCorrectCharPositions + state.currentCellPos).forEach {
                board.getChildAt(it).setDefaultColor(this)
            }
            myExample.dispatch { oldState ->
                oldState.copy(
                        lastCellPos = state.currentCellPos,
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

    private fun getCell(event: MotionEvent) : Int {
        for (index in 0 until board.childCount) {
            val cell = board.getChildAt(index)
            if (event.x <= cell.right
                    && event.x >= cell.left
                    && event.y >= cell.top
                    && event.y <= cell.bottom) {
                return index
            }
        }
        return -1
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

@Module
internal class MyModule {

    @Provides
    @Singleton
    fun provideMyExample(): Store<BoardState> {
        return Store(BoardState())
    }
}

@Singleton
@Component(modules = arrayOf(MyModule::class))
interface MyComponent {

    fun inject(mainActivity: MainActivity)

}