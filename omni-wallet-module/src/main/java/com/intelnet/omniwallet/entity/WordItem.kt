package com.intelnet.omniwallet.entity

data class WordItem(
    var name: String,
    var fill: String=""
) {
    companion object {
/*        fun generateListWord(): List<WordItem> {
            val seedCode =
                "yard impulse luxury drive today throw farm pepper survey wreck glass federal"
            return Pattern.compile(" ").split(seedCode).mapIndexed { i, name ->
                WordItem(i + 1, name)
            }.shuffled()
        }

        fun generateListBlank(): List<WordItem> {
            val seedCode =
                "yard impulse luxury drive today throw farm pepper survey wreck glass federal"
            return Pattern.compile(" ").split(seedCode).mapIndexed { i, name ->
                WordItem(i + 1, "")
            }
        }*/
    }
}