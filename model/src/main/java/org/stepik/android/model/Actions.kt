package org.stepik.android.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import org.stepik.android.model.util.readBoolean
import org.stepik.android.model.util.writeBoolean

class Actions(
    @SerializedName("vote")
    val vote: Boolean = false,
    @SerializedName("edit")
    val edit: Boolean = false,
    @SerializedName("delete")
    val delete: Boolean = false,
    @SerializedName("pin")
    val pin: Boolean = false,

    @SerializedName("test_section")
    val testSection: String? = null,
    @SerializedName("do_review")
    val doReview: String? = null,
    @SerializedName("edit_instructions")
    val editInstructions: String? = null
) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeBoolean(vote)
        parcel.writeBoolean(edit)
        parcel.writeBoolean(delete)
        parcel.writeBoolean(pin)
        parcel.writeString(testSection)
        parcel.writeString(doReview)
        parcel.writeString(editInstructions)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Actions> {
        override fun createFromParcel(parcel: Parcel): Actions = Actions(
                parcel.readBoolean(),
                parcel.readBoolean(),
                parcel.readBoolean(),
                parcel.readBoolean(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString()
        )

        override fun newArray(size: Int): Array<Actions?> = arrayOfNulls(size)
    }
}