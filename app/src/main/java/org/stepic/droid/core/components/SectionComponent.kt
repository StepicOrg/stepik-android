package org.stepic.droid.core.components

import dagger.Subcomponent
import org.stepic.droid.core.PerFragment
import org.stepic.droid.core.modules.SectionModule
import org.stepic.droid.ui.fragments.SectionsFragment

@PerFragment
@Subcomponent(modules = arrayOf(SectionModule::class))
interface SectionComponent {
    fun inject(sectionsFragment: SectionsFragment)
}