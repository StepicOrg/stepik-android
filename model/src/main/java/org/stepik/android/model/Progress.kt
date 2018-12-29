package org.stepik.android.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import org.stepik.android.model.util.readBoolean
import org.stepik.android.model.util.writeBoolean

data class Progress(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("last_viewed")
    val lastViewed: String? = null, //in SECONDS
    @SerializedName("score")
    var score: String? = null,
    @SerializedName("cost")
    val cost: Long = 0,
    @SerializedName("n_steps")
    val nSteps: Long = 0,
    @SerializedName("n_steps_passed")
    val nStepsPassed: Long = 0,
    @SerializedName("is_passed")
    val isPassed: Boolean = false
) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(lastViewed)
        parcel.writeString(score)
        parcel.writeLong(cost)
        parcel.writeLong(nSteps)
        parcel.writeLong(nStepsPassed)
        parcel.writeBoolean(isPassed)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Progress> {
        override fun createFromParcel(parcel: Parcel): Progress =
            Progress(
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readLong(),
                parcel.readLong(),
                parcel.readLong(),
                parcel.readBoolean()
            )

        override fun newArray(size: Int): Array<Progress?> = arrayOfNulls(size)
    }
}
