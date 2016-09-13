package org.stepic.droid.core

import dagger.Subcomponent
import org.stepic.droid.ui.fragments.FilterFragment

@PerFragment
@Subcomponent(modules = arrayOf(FilterModule::class))
interface FilterComponent {
    fun inject(filterFragment: FilterFragment)
}
