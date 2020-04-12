package org.stepic.droid.di.course_list

import dagger.Subcomponent
import org.stepik.android.view.fast_continue.ui.fragment.FastContinueFragment
import org.stepik.android.view.injection.course.CourseDataModule
import org.stepik.android.view.injection.course_payments.CoursePaymentsDataModule
import org.stepik.android.view.injection.last_step.LastStepDataModule
import org.stepik.android.view.injection.personal_deadlines.PersonalDeadlinesDataModule
import org.stepik.android.view.injection.progress.ProgressDataModule
import org.stepik.android.view.injection.section.SectionDataModule
import org.stepik.android.view.injection.unit.UnitDataModule

@CourseListScope
@Subcomponent(modules = [
    CourseListModule::class,

    CoursePaymentsDataModule::class,

    SectionDataModule::class,
    UnitDataModule::class,
    LastStepDataModule::class,
    ProgressDataModule::class,

    CourseDataModule::class,
    PersonalDeadlinesDataModule::class
])
interface CourseListComponent {

    @Subcomponent.Builder
    interface Builder {
        fun build(): CourseListComponent
    }

    fun inject(fragment: FastContinueFragment)
}
