package org.stepik.android.domain.review_session.model

import com.google.gson.annotations.SerializedName
import java.util.Date

data class ReviewSession(
    @SerializedName("id")
    val id: Long,
    @SerializedName("instruction")
    val instruction: Long,
    @SerializedName("submission")
    val submission: Long,

    @SerializedName("given_reviews")
    val givenReviews: List<Long>,
    @SerializedName("is_giving_started")
    val isGivingStarted: Boolean,
    @SerializedName("is_giving_finished")
    val isGivingFinished: Boolean,

    @SerializedName("taken_reviews")
    val takenReviews: List<Long>,
    @SerializedName("is_taking_started")
    val isTakingStarted: Boolean,
    @SerializedName("is_taking_finished")
    val isTakingFinished: Boolean,
    @SerializedName("is_taking_finished_by_teacher")
    val isTakingFinishedByTeacher: Boolean,
    @SerializedName("when_taking_finished_by_teacher")
    val whenTakingFinishedByTeacher: Date?,

    @SerializedName("is_review_available")
    val isReviewAvailable: Boolean,
    @SerializedName("is_finished")
    val isFinished: Boolean,

    @SerializedName("score")
    val score: Float,

//    @SerializedName("available_reviews_count")
//    val
)