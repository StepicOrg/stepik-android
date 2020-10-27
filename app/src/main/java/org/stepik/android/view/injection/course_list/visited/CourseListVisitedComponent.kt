package org.stepik.android.view.injection.course_list.visited

import dagger.Subcomponent
import org.stepik.android.view.course_list.ui.fragment.CourseListVisitedFragment
import org.stepik.android.view.injection.course.CourseDataModule
import org.stepik.android.view.injection.course_payments.CoursePaymentsDataModule
import org.stepik.android.view.injection.last_step.LastStepDataModule
import org.stepik.android.view.injection.profile.ProfileDataModule
import org.stepik.android.view.injection.tags.TagsDataModule
import org.stepik.android.view.injection.visited_courses.VisitedCoursesDataModule

@CourseListVisitedScope
@Subcomponent(modules = [
    CourseListVisitedModule::class,
    VisitedCoursesDataModule::class,
    CourseDataModule::class,
    CoursePaymentsDataModule::class,
    LastStepDataModule::class,
    TagsDataModule::class,
    ProfileDataModule::class]
)
interface CourseListVisitedComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): CourseListVisitedComponent
    }
    fun inject(courseListVisitedFragment: CourseListVisitedFragment)
}