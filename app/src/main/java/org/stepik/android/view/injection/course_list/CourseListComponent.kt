package org.stepik.android.view.injection.course_list

import dagger.Subcomponent
import org.stepik.android.view.course_list.activity.CourseListActivity
import org.stepik.android.view.course_list.ui.fragment.CourseCollectionFragment
import org.stepik.android.view.course_list.ui.fragment.CoursesQueryFragment
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

    fun inject(courseListActivity: CourseListActivity)
    fun inject(popularCoursesFragment: CoursesQueryFragment)
    fun inject(courseCollectionFragment: CourseCollectionFragment)
}