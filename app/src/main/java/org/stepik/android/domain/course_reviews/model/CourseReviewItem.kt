package org.stepik.android.domain.course_reviews.model

import org.stepik.android.model.user.User

sealed class CourseReviewItem {
    class Placeholder(
        val isPlaceholderForCurrentUser: Boolean = false
    ) : CourseReviewItem()

    data class Data(
        val courseReview: CourseReview,
        val user: User,
        val isCurrentUserReview: Boolean
    ) : CourseReviewItem()

    class ComposeBanner(
        val canWriteReview: Boolean
    ) : CourseReviewItem()
}