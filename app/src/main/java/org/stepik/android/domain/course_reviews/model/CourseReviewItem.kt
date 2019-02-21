package org.stepik.android.domain.course_reviews.model

import org.stepik.android.model.user.User

sealed class CourseReviewItem {
    object Placeholder : CourseReviewItem()

    class Data(
        val courseReview: CourseReview,
        val user: User
    ) : CourseReviewItem()
}