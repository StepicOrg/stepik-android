package org.stepik.android.view.injection.course_list

import dagger.Subcomponent
import org.stepik.android.view.course_list.activity.CourseListActivity
import org.stepik.android.view.injection.course.CourseDataModule
import org.stepik.android.view.injection.course_payments.CoursePaymentsDataModule

@Subcomponent(modules = [
    CourseListModule::class,
    CourseDataModule::class,
    CoursePaymentsDataModule::class
])
interface CourseListComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): CourseListComponent
    }

    fun inject(courseListActivity: CourseListActivity)
}