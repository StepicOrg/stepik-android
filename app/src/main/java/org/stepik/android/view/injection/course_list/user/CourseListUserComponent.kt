package org.stepik.android.view.injection.course_list.user

import dagger.Subcomponent
import org.stepik.android.view.course_list.ui.fragment.CourseListUserFragment
import org.stepik.android.view.course_list.ui.fragment.CourseListUserHorizontalFragment
import org.stepik.android.view.injection.course.CourseDataModule
import org.stepik.android.view.injection.course_payments.CoursePaymentsDataModule
import org.stepik.android.view.injection.last_step.LastStepDataModule
import org.stepik.android.view.injection.profile.ProfileDataModule

@CourseListUserScope
@Subcomponent(modules = [
    CourseListUserModule::class,
    CourseDataModule::class,
    CoursePaymentsDataModule::class,
    LastStepDataModule::class,
    ProfileDataModule::class]
)
interface CourseListUserComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): CourseListUserComponent
    }
    fun inject(courseListUserFragment: CourseListUserFragment)
    fun inject(courseListUserHorizontalFragment: CourseListUserHorizontalFragment)
}