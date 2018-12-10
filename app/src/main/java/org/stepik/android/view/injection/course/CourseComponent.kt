package org.stepik.android.view.injection.course

import dagger.BindsInstance
import dagger.Subcomponent
import org.stepic.droid.di.qualifiers.CourseId
import org.stepik.android.view.course.ui.activity.CourseActivity
import org.stepik.android.view.course_content.ui.fragment.CourseContentFragment
import org.stepik.android.view.course_info.ui.fragment.CourseInfoFragment
import org.stepik.android.view.injection.last_step.LastStepDataModule
import org.stepik.android.view.injection.lesson.LessonDataModule
import org.stepik.android.view.injection.progress.ProgressDataModule
import org.stepik.android.view.injection.section.SectionDataModule
import org.stepik.android.view.injection.unit.UnitDataModule
import org.stepik.android.view.injection.user.UserDataModule

@CourseScope
@Subcomponent(modules = [
    CourseModule::class,
    SectionDataModule::class,
    UnitDataModule::class,
    LessonDataModule::class,

    LastStepDataModule::class,
    ProgressDataModule::class,
    UserDataModule::class
])
interface CourseComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): CourseComponent

        @BindsInstance
        fun courseId(@CourseId courseId: Long): Builder
    }

    fun inject(courseActivity: CourseActivity)
    fun inject(courseInfoFragment: CourseInfoFragment)
    fun inject(courseContentFragment: CourseContentFragment)
}