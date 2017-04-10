package org.stepic.droid.di.filters

import dagger.Subcomponent
import org.stepic.droid.ui.fragments.FilterFragment

@FilterScope
@Subcomponent
interface FilterComponent {

    @Subcomponent.Builder
    interface Builder {
        fun build(): FilterComponent
    }

    fun inject(filterFragment: FilterFragment)
}
