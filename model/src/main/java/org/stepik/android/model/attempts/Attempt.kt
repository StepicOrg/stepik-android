package org.stepik.android.model.attempts

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class Attempt(
    @SerializedName("id")
    val id: Long = 0,
    @SerializedName("step")
    val step: Long = 0,
    @SerializedName("user")
    val user: Long = 0,

    @SerializedName("dataset")
    private val _dataset: DatasetWrapper? = null,
    @SerializedName("dataset_url")
    val datasetUrl: String? = null,

    @SerializedName("status")
    val status: String? = null,
    @SerializedName("time")
    val time: String? = null,

    @SerializedName("time_left")
    val timeLeft: String? = null
) : Parcelable {
    val dataset: Dataset?
        get() = _dataset?.dataset

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeLong(step)
        parcel.writeLong(user)
        parcel.writeParcelable(_dataset, flags)
        parcel.writeString(datasetUrl)
        parcel.writeString(status)
        parcel.writeString(time)
        parcel.writeString(timeLeft)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Attempt> {
        override fun createFromParcel(parcel: Parcel): Attempt =
            Attempt(
                parcel.readLong(),
                parcel.readLong(),
                parcel.readLong(),
                parcel.readParcelable(DatasetWrapper::class.java.classLoader),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString()
            )

        override fun newArray(size: Int): Array<Attempt?> =
            arrayOfNulls(size)
    }
}