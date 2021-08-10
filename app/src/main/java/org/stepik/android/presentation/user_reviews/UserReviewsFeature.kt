package org.stepik.android.presentation.user_reviews

import org.stepik.android.domain.course_reviews.model.CourseReview
import org.stepik.android.domain.user_reviews.model.UserCourseReviewItem

interface UserReviewsFeature {
    sealed class State {
        object Idle : State()
        object Loading : State()
        object Error : State()

        data class Content(val userCourseReviewItems: List<UserCourseReviewItem>) : State()
    }

    sealed class Message {
        data class InitMessage(val forceUpdate: Boolean = false) : Message()
        object InitListeningMessage : Message()
        data class FetchUserReviewsSuccess(val userCourseReviewItems: List<UserCourseReviewItem>) : Message()
        object FetchUserReviewsError : Message()
        data class NewReviewSubmission(val courseReview: CourseReview) : Message()
        data class EditReviewSubmission(val courseReview: CourseReview) : Message()
    }

    sealed class Action {
        object FetchUserReviews : Action()
        object ListenForUserReviews : Action()
        data class PublishChanges(val userCourseReviewItems: List<UserCourseReviewItem>) : Action()
        sealed class ViewAction : Action()
    }
}