package org.stepik.android.view.injection.course_benefits

import dagger.Subcomponent

@Subcomponent(modules = [
    CourseBenefitSummariesDataModule::class,
    CourseBenefitsDataModule::class,
    CourseBenefitByMonthsDataModule::class
])
interface CourseBenefitsComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): CourseBenefitsComponent
    }
}