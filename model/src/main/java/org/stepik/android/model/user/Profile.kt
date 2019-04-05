package org.stepik.android.model.user

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import org.stepik.android.model.util.readBoolean
import org.stepik.android.model.util.writeBoolean

data class Profile(
    @SerializedName("id")
    val id: Long = 0,

    @SerializedName("first_name")
    val firstName: String? = null,
    @SerializedName("last_name")
    val lastName: String? = null,

    @SerializedName("full_name")
    val fullName: String? = null,
    @SerializedName("short_bio")
    val shortBio: String? = null,

    val details: String? = null,
    val avatar: String? = null,

    @SerializedName("is_private")
    val isPrivate: Boolean = false,
    @SerializedName("is_guest")
    val isGuest: Boolean = false,

    @SerializedName("email_addresses")
    val emailAddresses: LongArray? = null
) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(firstName)
        parcel.writeString(lastName)
        parcel.writeString(fullName)
        parcel.writeString(shortBio)
        parcel.writeString(details)
        parcel.writeString(avatar)
        parcel.writeBoolean(isPrivate)
        parcel.writeBoolean(isGuest)
        parcel.writeLongArray(emailAddresses)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Profile> {
        override fun createFromParcel(parcel: Parcel): Profile =
            Profile(
                parcel.readLong(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readBoolean(),
                parcel.readBoolean(),
                parcel.createLongArray()
            )

        override fun newArray(size: Int): Array<Profile?> =
            arrayOfNulls(size)
    }
}