package org.stepik.android.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Reply(
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
    @SerializedName("blanks")
    val blanks: List<String>? = null,

    @SerializedName("solve_sql")
    val solveSql: String? = null,

    var tableChoices: List<TableChoiceAnswer>? = null //this is not serialize by default, because  field 'choices' is already created by different type
) : Parcelable

data class ReplyWrapper(val reply: Reply?)
