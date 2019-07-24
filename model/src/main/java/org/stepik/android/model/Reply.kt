package org.stepik.android.model

import com.google.gson.annotations.SerializedName

class Reply(
    @SerializedName("choices")
    val choices: List<Boolean>? = null,
    @SerializedName("text")
    val text: String? = null,
    @SerializedName("attachments")
    val attachments: List<Attachment>? = null,
    @SerializedName("formula")
    val formula: String? = null,
    @SerializedName("number")
    val number: String? = null,
    @SerializedName("ordering")
    val ordering: List<Int>? = null,
    @SerializedName("language")
    val language: String? = null,
    @SerializedName("code")
    val code: String? = null,

    @SerializedName("solve_sql")
    val solveSql: String? = null,

    var tableChoices: List<TableChoiceAnswer>? = null //this is not serialize by default, because  field 'choices' is already created by different type
)

class ReplyWrapper(val reply: Reply?)

data class TableChoiceAnswer(
    @SerializedName("name_row")
    val nameRow: String,
    @SerializedName("columns")
    val columns: List<Cell>
) {
    data class Cell(
        @SerializedName("name")
        val name: String,
        @SerializedName("answer")
        var answer: Boolean
    )
}
