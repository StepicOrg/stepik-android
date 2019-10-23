package org.stepik.android.domain.course_reviews.model

import org.stepik.android.model.user.User
import ru.nobird.android.core.model.Identifiable

sealed class CourseReviewItem {
    data class Placeholder(
        val isPlaceholderForCurrentUser: Boolean = false
    ) : CourseReviewItem()

    data class Data(
        val courseReview: CourseReview,
        val user: User,
        val isCurrentUserReview: Boolean
    ) : CourseReviewItem(), Identifiable<Long> {
        override val id: Long =
            courseReview.id
    }

    data class ComposeBanner(
        val canWriteReview: Boolean,
        val isReviewsEmpty: Boolean = false
    ) : CourseReviewItem()
}