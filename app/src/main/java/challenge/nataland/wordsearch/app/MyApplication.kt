package challenge.nataland.wordsearch.app

import android.app.Application
import challenge.nataland.wordsearch.game.DaggerGameComponent
import challenge.nataland.wordsearch.game.GameComponent
import challenge.nataland.wordsearch.game.GameModule

class MyApplication : Application() {
    lateinit var myComponent: GameComponent

    override fun onCreate() {
        super.onCreate()
        myComponent = createGameComponent()
    }

    private fun createGameComponent(): GameComponent {
        return DaggerGameComponent.builder()
                .gameModule(GameModule())
                .build()
    }
}