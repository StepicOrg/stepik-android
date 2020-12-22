package org.stepik.android.view.injection.course_list.visited

import dagger.Subcomponent
import org.stepik.android.view.course_list.ui.fragment.CourseListVisitedFragment
import org.stepik.android.view.course_list.ui.fragment.CourseListVisitedHorizontalFragment
import org.stepik.android.view.injection.course.CourseDataModule
import org.stepik.android.view.injection.course_payments.CoursePaymentsDataModule
import org.stepik.android.view.injection.last_step.LastStepDataModule
import org.stepik.android.view.injection.profile.ProfileDataModule

@CourseListVisitedScope
@Subcomponent(modules = [
    CourseListVisitedModule::class,
    CourseDataModule::class,
    CoursePaymentsDataModule::class,
    LastStepDataModule::class,
    ProfileDataModule::class]
)
interface CourseListVisitedComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): CourseListVisitedComponent
    }
    fun inject(courseListVisitedHorizontalFragment: CourseListVisitedHorizontalFragment)
    fun inject(courseListVisitedFragment: CourseListVisitedFragment)
}