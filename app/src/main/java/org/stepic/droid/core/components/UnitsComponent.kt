package org.stepic.droid.core.components

import dagger.Subcomponent
import org.stepic.droid.core.PerFragment
import org.stepic.droid.core.modules.UnitsModule
import org.stepic.droid.ui.fragments.UnitsFragment

@PerFragment
@Subcomponent(modules = arrayOf(UnitsModule::class))
interface UnitsComponent {
    fun inject(unitsFragment: UnitsFragment)
}