package org.stepik.android.view.injection.course_list.collection

import dagger.Subcomponent
import org.stepik.android.view.course_list.ui.fragment.CourseListCollectionFragment
import org.stepik.android.view.injection.course.CourseDataModule
import org.stepik.android.view.injection.course_payments.CoursePaymentsDataModule
import org.stepik.android.view.injection.last_step.LastStepDataModule
import org.stepik.android.view.injection.profile.ProfileDataModule

@CourseListCollectionScope
@Subcomponent(modules = [
    CourseListCollectionModule::class,
    CourseDataModule::class,
    CoursePaymentsDataModule::class,
    LastStepDataModule::class,
    ProfileDataModule::class]
)
interface CourseListCollectionComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): CourseListCollectionComponent
    }
    fun inject(courseListCollectionFragment: CourseListCollectionFragment)
}