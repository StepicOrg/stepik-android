package org.stepic.droid.core.components

import dagger.Subcomponent
import org.stepic.droid.core.PerFragment
import org.stepic.droid.core.modules.FilterModule
import org.stepic.droid.ui.fragments.FilterFragment

@PerFragment
@Subcomponent(modules = arrayOf(FilterModule::class))
interface FilterComponent {
    fun inject(filterFragment: FilterFragment)
}
