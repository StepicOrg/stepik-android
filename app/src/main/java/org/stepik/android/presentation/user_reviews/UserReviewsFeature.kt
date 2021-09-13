package org.stepik.android.presentation.user_reviews

import org.stepik.android.domain.course_reviews.model.CourseReview
import org.stepik.android.domain.user_reviews.model.UserCourseReviewItem
import org.stepik.android.domain.user_reviews.model.UserCourseReviewsResult
import org.stepik.android.model.Course

interface UserReviewsFeature {
    sealed class State {
        object Idle : State()
        object Loading : State()
        object Error : State()
        object Empty : State()

        data class Content(val userCourseReviewsResult: UserCourseReviewsResult) : State()
    }

    sealed class Message {
        data class InitMessage(val forceUpdate: Boolean = false) : Message()
        data class FetchUserReviewsSuccess(val userCourseReviewsResult: UserCourseReviewsResult) : Message()
        object FetchUserReviewsError : Message()
        object ScreenOpenedMessage : Message()

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

        /**
         * Enrolled operation
         */
        data class EnrolledCourseMessage(val course: Course) : Message()
        data class EnrolledReviewedCourseMessage(val reviewedItem: UserCourseReviewItem.ReviewedItem) : Message()
        data class EnrolledPotentialReviewMessage(val potentialReviewItem: UserCourseReviewItem.PotentialReviewItem) : Message()
    }

    sealed class Action {
        object FetchUserReviews : Action()
        object LogScreenOpenedEvent : Action()
        data class DeleteReview(val courseReview: CourseReview) : Action()
        data class FetchEnrolledCourseInfo(val course: Course) : Action()
        sealed class ViewAction : Action() {
            object ShowDeleteSuccessSnackbar : ViewAction()
            object ShowDeleteFailureSnackbar : ViewAction()
        }
    }
}