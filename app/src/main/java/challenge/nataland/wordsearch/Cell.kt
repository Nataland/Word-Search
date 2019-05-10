package challenge.nataland.wordsearch

data class Cell(var content: String = "", var fullWord: String = "") {
    fun empty() {
        content = ""
        fullWord = ""
    }
}