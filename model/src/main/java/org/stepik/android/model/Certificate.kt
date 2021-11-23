package org.stepik.android.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.util.Date

@Parcelize
data class Certificate(
    @SerializedName("id")
    val id: Long,
    @SerializedName("user")
    val user: Long,
    @SerializedName("course")
    val course: Long,

    @SerializedName("issue_date")
    val issueDate: Date? = null,
    @SerializedName("update_date")
    val updateDate: Date? = null,

    @SerializedName("grade")
    val grade: String? = null,
    @SerializedName("type")
    val type: Type? = null,
    @SerializedName("url")
    val url: String? = null,

    @SerializedName("user_rank")
    val userRank: Long?,
    @SerializedName("user_rank_max")
    val userRankMax: Long?,
    @SerializedName("leaderboard_size")
    val leaderboardSize: Long?,
    @SerializedName("preview_url")
    val previewUrl: String?,

    @SerializedName("saved_fullname")
    val savedFullName: String? = null,
    @SerializedName("edits_count")
    val editsCount: Int,
    @SerializedName("allowed_edits_count")
    val allowedEditsCount: Int
) : Parcelable {

    /*
    Add new in the end, because serialization depends on order.
    */
    enum class Type {
        @SerializedName("regular")
        REGULAR,
        @SerializedName("distinction")
        DISTINCTION
    }
}