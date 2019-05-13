package challenge.nataland.wordsearch.board

data class Cell(var content: String = "", var fullWord: String = "") {
    fun empty() {
        content = ""
        fullWord = ""
    }
}