package org.stepik.android.view.injection.course_revenue

import dagger.Subcomponent
import org.stepik.android.view.course_revenue.ui.activity.CourseRevenueActivity
import org.stepik.android.view.injection.user.UserDataModule

@Subcomponent(modules = [
    CourseRevenuePresentationModule::class,
    CourseBenefitSummariesDataModule::class,
    CourseBenefitsDataModule::class,
    CourseBenefitByMonthsDataModule::class,
    CourseBeneficiariesDataModule::class,
    UserDataModule::class
])
interface CourseRevenueComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): CourseRevenueComponent
    }

    fun inject(courseRevenueActivity: CourseRevenueActivity)
}