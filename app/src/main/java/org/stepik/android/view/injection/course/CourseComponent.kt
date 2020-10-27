package org.stepik.android.view.injection.course

import dagger.BindsInstance
import dagger.Subcomponent
import org.stepic.droid.di.qualifiers.CourseId
import org.stepik.android.view.course_content.ui.fragment.CourseContentFragment
import org.stepik.android.view.course_info.ui.fragment.CourseInfoFragment
import org.stepik.android.view.course_reviews.ui.fragment.CourseReviewsFragment
import org.stepik.android.view.injection.calendar.CalendarDataModule
import org.stepik.android.view.injection.course_calendar.CourseCalendarDataModule
import org.stepik.android.view.injection.course_payments.CoursePaymentsDataModule
import org.stepik.android.view.injection.course_reviews.CourseReviewsDataModule
import org.stepik.android.view.injection.last_step.LastStepDataModule
import org.stepik.android.view.injection.notification.NotificationDataModule
import org.stepik.android.view.injection.personal_deadlines.PersonalDeadlinesDataModule
import org.stepik.android.view.injection.profile.ProfileDataModule
import org.stepik.android.view.injection.progress.ProgressDataModule
import org.stepik.android.view.injection.step.StepDataModule
import org.stepik.android.view.injection.user.UserDataModule

@CourseScope
@Subcomponent(modules = [
    CourseModule::class,
    CourseDataModule::class,

    LastStepDataModule::class,
    ProgressDataModule::class,
    UserDataModule::class,
    StepDataModule::class,

    CourseDataModule::class,
    CoursePaymentsDataModule::class,
    CourseReviewsDataModule::class,
    PersonalDeadlinesDataModule::class,
    NotificationDataModule::class,

    CourseCalendarDataModule::class,
    CalendarDataModule::class,
    ProfileDataModule::class
])
interface CourseComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): CourseComponent

        @BindsInstance
        fun courseId(@CourseId courseId: Long): Builder
    }

    fun coursePresentationComponentBuilder(): CoursePresentationComponent.Builder

    fun inject(courseInfoFragment: CourseInfoFragment)
    fun inject(courseReviewsFragment: CourseReviewsFragment)
    fun inject(courseContentFragment: CourseContentFragment)
}