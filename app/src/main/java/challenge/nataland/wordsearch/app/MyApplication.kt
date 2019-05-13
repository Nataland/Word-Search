package challenge.nataland.wordsearch.app

import android.app.Application
import challenge.nataland.wordsearch.DaggerMyComponent
import challenge.nataland.wordsearch.game.MyComponent
import challenge.nataland.wordsearch.game.MyModule

class MyApplication : Application() {
    lateinit var myComponent: MyComponent

    override fun onCreate() {
        super.onCreate()
        myComponent = createMyComponent()
    }

    private fun createMyComponent(): MyComponent {
        return DaggerMyComponent.builder()
                .myModule(MyModule())
                .build()
    }

}