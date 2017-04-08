package org.stepic.droid.di.course

import dagger.Subcomponent
import org.stepic.droid.ui.fragments.CourseDetailFragment


@CourseAndSectionsScope
@Subcomponent
interface CourseDetailComponent {

    @Subcomponent.Builder
    interface Builder {
        fun build(): CourseDetailComponent
    }

    fun inject(courseDetailFragment: CourseDetailFragment)
}