package org.stepik.android.view.debug.ui

import androidx.fragment.app.Fragment
import org.stepik.android.domain.course_reviews.analytic.CourseReviewViewSource
import org.stepik.android.view.course_reviews.ui.dialog.ComposeCourseReviewDialogFragment
import ru.nobird.android.view.base.ui.extension.showIfNotExists

object ComposeCourseReviewPreviewData {
    fun show(host: Fragment) {
        ComposeCourseReviewDialogFragment
            .newInstance(
                courseId = -1L,
                courseReviewViewSource = CourseReviewViewSource.COURSE_REVIEWS_SOURCE,
                courseReview = null,
                courseRating = 4f
            )
            .showIfNotExists(host.childFragmentManager, ComposeCourseReviewDialogFragment.TAG)
    }
}
