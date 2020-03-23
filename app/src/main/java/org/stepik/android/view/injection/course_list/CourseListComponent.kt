package org.stepik.android.view.injection.course_list

import dagger.Subcomponent
import org.stepik.android.view.course_list.ui.fragment.CourseListCollectionFragment
import org.stepik.android.view.course_list.ui.fragment.CourseListQueryFragment
import org.stepik.android.view.injection.course.CourseDataModule
import org.stepik.android.view.injection.course_payments.CoursePaymentsDataModule
import org.stepik.android.view.injection.last_step.LastStepDataModule

@CourseListScope
@Subcomponent(modules = [
    CourseListModule::class,
    CourseDataModule::class,
    CoursePaymentsDataModule::class,
    LastStepDataModule::class
])
interface CourseListComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): CourseListComponent
    }

    fun inject(courseListQueryFragment: CourseListQueryFragment)
    fun inject(courseListCollectionFragment: CourseListCollectionFragment)
}