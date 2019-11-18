package org.stepik.android.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class TableChoiceAnswer(
    @SerializedName("name_row")
    val nameRow: String,
    @SerializedName("columns")
    val columns: List<Cell>
) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(nameRow)
        parcel.writeTypedList(columns)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<TableChoiceAnswer> {
        override fun createFromParcel(parcel: Parcel): TableChoiceAnswer =
            TableChoiceAnswer(
                parcel.readString()!!,
                parcel.createTypedArrayList(Cell)!!
            )

        override fun newArray(size: Int): Array<TableChoiceAnswer?> =
            arrayOfNulls(size)
    }
}