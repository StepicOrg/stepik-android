package org.stepik.android.domain.user_reviews.model

import org.stepik.android.domain.course_reviews.model.CourseReview

sealed class UserCourseReviewOperation {
    data class CreateReviewOperation(val courseReview: CourseReview) : UserCourseReviewOperation()
    data class EditReviewOperation(val courseReview: CourseReview) : UserCourseReviewOperation()
    data class RemoveReviewOperation(val courseReview: CourseReview) : UserCourseReviewOperation()
}