package org.stepic.droid.di.course

import dagger.Subcomponent
import org.stepic.droid.ui.fragments.CourseDetailFragment
import org.stepic.droid.ui.fragments.SectionsFragment


@CourseAndSectionsScope
@Subcomponent(modules = arrayOf(CourseAndSectionsModule::class))
interface CourseComponent {

    @Subcomponent.Builder
    interface Builder {
        fun build(): CourseComponent
    }

    fun inject(courseDetailFragment: CourseDetailFragment)

    fun inject(sectionsFragment: SectionsFragment)
}