package org.stepik.android.model.user

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.util.Date

@Parcelize
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
) : Parcelable