package org.stepik.android.view.injection.course_list.search_result

import dagger.Subcomponent
import org.stepik.android.view.course_list.ui.fragment.CourseListSearchFragment
import org.stepik.android.view.course_list.ui.fragment.CourseListTagFragment
import org.stepik.android.view.injection.course.CourseDataModule
import org.stepik.android.view.injection.course_payments.CoursePaymentsDataModule
import org.stepik.android.view.injection.last_step.LastStepDataModule
import org.stepik.android.view.injection.profile.ProfileDataModule
import org.stepik.android.view.injection.tags.TagsDataModule

@CourseListSearchResultScope
@Subcomponent(modules = [
    CourseListSearchResultModule::class,
    CourseDataModule::class,
    CoursePaymentsDataModule::class,
    LastStepDataModule::class,
    TagsDataModule::class,
    ProfileDataModule::class
])
interface CourseListSearchResultComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): CourseListSearchResultComponent
    }

    fun inject(courseListTagFragment: CourseListTagFragment)
    fun inject(courseListSearchFragment: CourseListSearchFragment)
}