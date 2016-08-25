package org.stepic.droid.core

import dagger.Subcomponent
import org.stepic.droid.ui.fragments.SectionFragment

@PerFragment
@Subcomponent(modules = arrayOf(SectionModule::class))
interface SectionComponent {
    fun inject(sectionFragment: SectionFragment)
}