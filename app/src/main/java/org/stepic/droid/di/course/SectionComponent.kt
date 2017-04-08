package org.stepic.droid.di.course

import dagger.Subcomponent
import org.stepic.droid.ui.fragments.SectionsFragment

@CourseAndSectionsScope
@Subcomponent
interface SectionComponent {

    @Subcomponent.Builder
    interface Builder {
        fun build(): SectionComponent
    }

    fun inject(sectionsFragment: SectionsFragment)
}