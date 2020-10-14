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
    @SerializedName("cover")
    val cover: String? = null,

    @SerializedName("is_private")
    val isPrivate: Boolean = false,
    @SerializedName("is_guest")
    val isGuest: Boolean = false,
    @SerializedName("is_organization")
    val isOrganization: Boolean = false,

    @SerializedName("social_profiles")
    val socialProfiles: List<Long> = emptyList(),

    @SerializedName("knowledge")
    val knowledge: Long = 0,
    @SerializedName("knowledge_rank")
    val knowledgeRank: Long = 0,
    @SerializedName("reputation")
    val reputation: Long = 0,
    @SerializedName("reputation_rank")
    val reputationRank: Long = 0,

    @SerializedName("created_courses_count")
    val createdCoursesCount: Long = 0,
    @SerializedName("followers_count")
    val followersCount: Long = 0,
    @SerializedName("issued_certificates_count")
    val issuedCertificatesCount: Long = 0,

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
        parcel.writeString(cover)
        parcel.writeBoolean(isPrivate)
        parcel.writeBoolean(isGuest)
        parcel.writeBoolean(isOrganization)

        parcel.writeList(socialProfiles)
        parcel.writeLong(knowledge)
        parcel.writeLong(knowledgeRank)
        parcel.writeLong(reputation)
        parcel.writeLong(reputationRank)

        parcel.writeLong(createdCoursesCount)
        parcel.writeLong(followersCount)
        parcel.writeLong(issuedCertificatesCount)

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
                parcel.readString(),
                parcel.readBoolean(),
                parcel.readBoolean(),
                parcel.readBoolean(),
                mutableListOf<Long>().apply { parcel.readList(this as List<Long>, Long::class.java.classLoader) },
                parcel.readLong(),
                parcel.readLong(),
                parcel.readLong(),
                parcel.readLong(),

                parcel.readLong(),
                parcel.readLong(),
                parcel.readLong(),

                parcel.readDate()
            )

        override fun newArray(size: Int): Array<User?> =
            arrayOfNulls(size)
    }
}