package org.stepik.android.view.injection.course_reviews

import dagger.Subcomponent
import org.stepik.android.view.course_reviews.ui.dialog.ComposeCourseReviewDialogFragment

@Subcomponent(modules = [
    CourseReviewsDataModule::class,
    ComposeCourseReviewModule::class
])
interface ComposeCourseReviewComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): ComposeCourseReviewComponent
    }

    fun inject(composeCourseReviewDialogFragment: ComposeCourseReviewDialogFragment)
}