package org.stepik.android.domain.wishlist.analytic

import org.stepik.android.domain.base.analytic.AnalyticEvent
import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.model.Course

class CourseWishlistAddedEvent(
    course: Course,
    source: CourseViewSource
) : AnalyticEvent {
    companion object {
        private const val PARAM_COURSE = "course"
        private const val PARAM_TITLE = "title"
        private const val PARAM_IS_PAID = "is_paid"
        private const val PARAM_SOURCE = "source"
    }

    override val name: String =
        "Course wishlist added"

    override val params: Map<String, Any> =
        mapOf(
            PARAM_COURSE to course.id,
            PARAM_TITLE to course.title.toString(),
            PARAM_IS_PAID to course.isPaid,
            PARAM_SOURCE to source.name
        ) + source.params.mapKeys { "${PARAM_SOURCE}_${it.key}" }
}