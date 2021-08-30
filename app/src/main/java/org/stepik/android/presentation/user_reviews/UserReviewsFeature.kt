package org.stepik.android.presentation.user_reviews

import org.stepik.android.domain.course_reviews.model.CourseReview
import org.stepik.android.domain.user_reviews.model.UserCourseReviewsResult

interface UserReviewsFeature {
    sealed class State {
        object Idle : State()
        object Loading : State()
        object Error : State()

        data class Content(val userCourseReviewsResult: UserCourseReviewsResult) : State()
    }

    sealed class Message {
        data class InitMessage(val forceUpdate: Boolean = false) : Message()
        object InitListeningMessage : Message()
        data class FetchUserReviewsSuccess(val userCourseReviewsResult: UserCourseReviewsResult) : Message()
        object FetchUserReviewsError : Message()

        /***
         * Handling operations from course screen
         */
        data class NewReviewSubmission(val courseReview: CourseReview) : Message()
        data class EditReviewSubmission(val courseReview: CourseReview) : Message()
        data class DeletedReviewSubmission(val courseReview: CourseReview) : Message()

        /**
         * Deletion operations
         */
        data class DeletedReviewUserReviews(val courseReview: CourseReview) : Message()
        data class DeletedReviewUserReviewsSuccess(val courseReview: CourseReview) : Message()
        data class DeletedReviewUserReviewsError(val courseReview: CourseReview) : Message()
    }

    sealed class Action {
        object FetchUserReviews : Action()
        object ListenForUserReviews : Action()
        data class DeleteReview(val courseReview: CourseReview) : Action()
        data class PublishChanges(val userCourseReviewsResult: UserCourseReviewsResult) : Action()
        sealed class ViewAction : Action()
    }
}