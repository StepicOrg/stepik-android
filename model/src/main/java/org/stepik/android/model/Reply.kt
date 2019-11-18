package org.stepik.android.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

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

    @SerializedName("solve_sql")
    val solveSql: String? = null,

    var tableChoices: List<TableChoiceAnswer>? = null //this is not serialize by default, because  field 'choices' is already created by different type
) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeList(choices)
        parcel.writeString(text)
        parcel.writeTypedList(attachments)
        parcel.writeString(formula)
        parcel.writeString(number)
        parcel.writeList(ordering)
        parcel.writeString(language)
        parcel.writeString(code)
        parcel.writeString(solveSql)
        parcel.writeTypedList(tableChoices)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Reply> {
        override fun createFromParcel(parcel: Parcel): Reply =
            Reply(
                parcel.readArrayList(Boolean::class.java.classLoader) as? List<Boolean>,
                parcel.readString(),
                parcel.createTypedArrayList(Attachment),
                parcel.readString(),
                parcel.readString(),
                parcel.readArrayList(Int::class.java.classLoader) as? List<Int>,
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.createTypedArrayList(TableChoiceAnswer)
            )

        override fun newArray(size: Int): Array<Reply?> =
            arrayOfNulls(size)
    }
}

data class ReplyWrapper(val reply: Reply?)
