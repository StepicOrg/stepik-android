package org.stepik.android.model.learning.reply

import com.google.gson.annotations.SerializedName

data class TableChoiceAnswer(
        @SerializedName("name_row")
        val nameRow: String,
        val columns: List<Cell>
) {
    data class Cell(
            val name: String,
            var answer: Boolean
    )
}
