package org.stepik.android.model.user

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import org.stepik.android.model.util.readBoolean
import org.stepik.android.model.util.readDate
import org.stepik.android.model.util.writeBoolean
import org.stepik.android.model.util.writeDate
import java.util.Date

data class User(
        @SerializedName("id")
        val id: Long = 0,
        @SerializedName("profile")
        val profile: Long = 0,

        @SerializedName("first_name")
        val firstName: String? = null,
        @SerializedName("last_name")
        val lastName: String? = null,

        @SerializedName("full_name")
        val fullName: String? = null,
        @SerializedName("short_bio")
        val shortBio: String? = null,

        @SerializedName("details")
        val details: String? = null,
        @SerializedName("avatar")
        val avatar: String? = null,

        @SerializedName("is_private")
        val isPrivate: Boolean = false,
        @SerializedName("join_date")
        val joinDate: Date?
) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeLong(profile)
        parcel.writeString(firstName)
        parcel.writeString(lastName)
        parcel.writeString(fullName)
        parcel.writeString(shortBio)
        parcel.writeString(details)
        parcel.writeString(avatar)
        parcel.writeBoolean(isPrivate)
        parcel.writeDate(joinDate)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel) =
            User(
                parcel.readLong(),
                parcel.readLong(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readBoolean(),
                parcel.readDate()
            )

        override fun newArray(size: Int): Array<User?> =
            arrayOfNulls(size)
    }
}