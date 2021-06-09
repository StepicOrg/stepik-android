package org.stepik.android.view.injection.course_complete

import dagger.Subcomponent
import org.stepik.android.view.course_complete.ui.dialog.CourseCompleteBottomSheetDialogFragment
import org.stepik.android.view.injection.certificate.CertificateDataModule
import org.stepik.android.view.injection.course_reviews.CourseReviewsDataModule
import org.stepik.android.view.injection.progress.ProgressDataModule

@Subcomponent(
    modules = [
        CourseCompletePresentationModule::class,
        ProgressDataModule::class,
        CertificateDataModule::class,
        CourseReviewsDataModule::class
    ]
)
interface CourseCompleteComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): CourseCompleteComponent
    }

    fun inject(courseCompleteBottomSheetDialogFragment: CourseCompleteBottomSheetDialogFragment)
}