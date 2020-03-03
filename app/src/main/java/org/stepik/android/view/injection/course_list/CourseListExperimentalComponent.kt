package org.stepik.android.view.injection.course_list

import dagger.Subcomponent
import org.stepik.android.view.course_list.activity.CourseListPlaygroundActivity
import org.stepik.android.view.injection.course.CourseDataModule
import org.stepik.android.view.injection.course_payments.CoursePaymentsDataModule

@Subcomponent(modules = [
    CourseListPlaygroundModule::class,
    CourseListModule::class,
    CourseDataModule::class,
    CoursePaymentsDataModule::class
])
interface CourseListExperimentalComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): CourseListExperimentalComponent
    }

    fun inject(courseListPlaygroundActivity: CourseListPlaygroundActivity)
}