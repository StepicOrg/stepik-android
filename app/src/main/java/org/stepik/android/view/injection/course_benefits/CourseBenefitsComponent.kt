package org.stepik.android.view.injection.course_benefits

import dagger.Subcomponent
import org.stepik.android.view.course_benefits.ui.activity.CourseBenefitsActivity
import org.stepik.android.view.injection.user.UserDataModule

@Subcomponent(modules = [
    CourseBenefitsPresentationModule::class,
    CourseBenefitSummariesDataModule::class,
    CourseBenefitsDataModule::class,
    CourseBenefitByMonthsDataModule::class,
    UserDataModule::class
])
interface CourseBenefitsComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): CourseBenefitsComponent
    }

    fun inject(courseBenefitsActivity: CourseBenefitsActivity)
}