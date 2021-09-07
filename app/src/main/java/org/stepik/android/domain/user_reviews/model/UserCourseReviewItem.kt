package org.stepik.android.domain.user_reviews.model

import org.stepik.android.domain.course_reviews.model.CourseReview
import org.stepik.android.model.Course
import ru.nobird.android.core.model.Identifiable

sealed class UserCourseReviewItem {
    data class PotentialReviewHeader(val potentialReviewCount: Int) : UserCourseReviewItem()
    data class PotentialReviewItem(val course: Course) : UserCourseReviewItem(), Identifiable<Long> {
        override val id: Long =
            course.id
    }

    data class ReviewedHeader(val reviewedCount: Int) : UserCourseReviewItem()
    data class ReviewedItem(val course: Course, val courseReview: CourseReview) : UserCourseReviewItem(), Identifiable<Long> {
        override val id: Long =
            courseReview.course
    }

    data class Placeholder(val courseId: Long = -1L, val course: Course? = null) : UserCourseReviewItem(), Identifiable<Long> {
        override val id: Long =
            courseId
    }
}