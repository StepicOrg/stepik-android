package org.stepik.android.domain.review_session.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import ru.nobird.android.core.model.Identifiable
import java.util.Date

@Entity
data class ReviewSession(
    @PrimaryKey
    @SerializedName("id")
    override val id: Long,
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

    @SerializedName("available_reviews_count")
    val availableReviewsCount: Int?,

    @SerializedName("active_review")
    val activeReview: Long?,

    @Embedded
    @SerializedName("actions")
    val actions: Actions
) : Identifiable<Long> {
    data class Actions(
        @SerializedName("finish")
        val finish: Boolean
    )
}