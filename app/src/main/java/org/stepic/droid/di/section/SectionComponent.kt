package org.stepic.droid.di.section

import dagger.Subcomponent
import org.stepic.droid.ui.fragments.UnitsFragment

@Subcomponent
interface SectionComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): SectionComponent
    }

    fun inject(unitsFragment: UnitsFragment)
}