package org.stepic.droid.di.course_general

import dagger.Subcomponent
import org.stepic.droid.di.course_list.CourseGeneralScope
import org.stepic.droid.di.course_list.CourseListComponent

@CourseGeneralScope
@Subcomponent(modules = [
    CourseGeneralModule::class
])
interface CourseGeneralComponent {

    @Subcomponent.Builder
    interface Builder {
        fun build(): CourseGeneralComponent
    }

    fun courseListComponentBuilder(): CourseListComponent.Builder

}
