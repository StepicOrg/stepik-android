package org.stepic.droid.di.course_list

import dagger.Subcomponent
import org.stepic.droid.base.CoursesDatabaseFragmentBase
import org.stepic.droid.di.tags.TagComponent
import org.stepic.droid.ui.custom.CoursesCarouselView
import org.stepic.droid.ui.fragments.CourseCollectionFragment
import org.stepic.droid.ui.fragments.CourseListFragmentBase
import org.stepic.droid.ui.fragments.CourseSearchFragment
import org.stepic.droid.ui.fragments.FastContinueFragment
import org.stepik.android.view.injection.billing.BillingDataModule
import org.stepik.android.view.injection.course.CourseDataModule
import org.stepik.android.view.injection.course_list.CourseListDataModule
import org.stepik.android.view.injection.course_payments.CoursePaymentsDataModule
import org.stepik.android.view.injection.last_step.LastStepDataModule
import org.stepik.android.view.injection.personal_deadlines.PersonalDeadlinesDataModule
import org.stepik.android.view.injection.progress.ProgressDataModule
import org.stepik.android.view.injection.section.SectionDataModule
import org.stepik.android.view.injection.unit.UnitDataModule

@CourseListScope
@Subcomponent(modules = [
    CourseListModule::class,

    BillingDataModule::class,
    CoursePaymentsDataModule::class,

    SectionDataModule::class,
    UnitDataModule::class,
    LastStepDataModule::class,
    ProgressDataModule::class,

    CourseListDataModule::class,
    PersonalDeadlinesDataModule::class
])
interface CourseListComponent {

    @Subcomponent.Builder
    interface Builder {
        fun build(): CourseListComponent
    }

    fun tagComponentBuilder(): TagComponent.Builder

    fun inject(fragment: CoursesDatabaseFragmentBase)

    fun inject(fragment: CourseListFragmentBase)

    fun inject(fragment: CourseCollectionFragment)

    fun inject(fragment: CourseSearchFragment)

    fun inject(view: CoursesCarouselView)

    fun inject(fragment: FastContinueFragment)
}
