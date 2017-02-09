package org.stepic.droid.model

data class TableChoiceAnswer(val name_row: String, val columns: List<Cell>) {
    companion object {
        data class Cell(val name: String, var answer: Boolean)
    }
}
