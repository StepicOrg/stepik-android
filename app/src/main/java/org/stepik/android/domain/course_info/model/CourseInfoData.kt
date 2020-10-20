package org.stepik.android.domain.course_info.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.stepik.android.model.user.User
import org.stepik.android.view.video_player.model.VideoPlayerMediaData

/**
 * Data class to represent course info state
 * null field means no block will be displayed
 * null items in instructor block means that users not loaded yet
 */
@Parcelize
data class CourseInfoData(
    val organization: User? = null,
    val videoMediaData: VideoPlayerMediaData? = null,
    val about: String? = null,
    val requirements: String? = null,
    val targetAudience: String? = null,
    val timeToComplete: Long = 0,
    val instructors: List<User?>? = null,
    val language: String? = null,
    val certificate: Certificate? = null,
    val learnersCount: Long = 0
) : Parcelable {
    @Parcelize
    data class Certificate(
        val title: String,
        val distinctionThreshold: Long,
        val regularThreshold: Long
    ) : Parcelable
}