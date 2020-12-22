package org.stepik.android.view.injection.course_list.query

import dagger.Subcomponent
import org.stepik.android.view.course_list.ui.fragment.CourseListPopularFragment
import org.stepik.android.view.course_list.ui.fragment.CourseListQueryFragment
import org.stepik.android.view.injection.course.CourseDataModule
import org.stepik.android.view.injection.course_payments.CoursePaymentsDataModule
import org.stepik.android.view.injection.last_step.LastStepDataModule
import org.stepik.android.view.injection.profile.ProfileDataModule

@CourseListQueryScope
@Subcomponent(modules = [
    CourseListQueryModule::class,
    CourseDataModule::class,
    CoursePaymentsDataModule::class,
    LastStepDataModule::class,
    ProfileDataModule::class]
)
interface CourseListQueryComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): CourseListQueryComponent
    }
    fun inject(courseListQueryFragment: CourseListQueryFragment)
    fun inject(courseListPopularFragment: CourseListPopularFragment)
}