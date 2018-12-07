package org.stepic.droid.di.course

import dagger.Subcomponent
import org.stepic.droid.ui.fragments.SectionsFragment


@CourseAndSectionsScope
@Subcomponent
interface CourseComponent {

    @Subcomponent.Builder
    interface Builder {
        fun build(): CourseComponent
    }

    fun inject(sectionsFragment: SectionsFragment)
}