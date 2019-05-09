package challenge.nataland.wordsearch

data class Cell(var content: String = "", var fullWord: String = "", var found: Boolean = false) {
    fun empty() {
        content = ""
        fullWord = ""
    }
}