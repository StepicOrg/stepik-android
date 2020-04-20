package org.stepik.android.view.injection.course

import dagger.Subcomponent
import org.stepik.android.view.course.ui.activity.CourseActivity

@CoursePresentationScope
@Subcomponent(modules = [CoursePresentationModule::class])
interface CoursePresentationComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): CoursePresentationComponent
    }
    fun inject(courseActivity: CourseActivity)
}