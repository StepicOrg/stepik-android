package org.stepik.android.view.injection.settings

import dagger.Subcomponent
import org.stepik.android.view.settings.ui.fragment.SettingsFragment

@Subcomponent(modules = [SettingsModule::class])
interface SettingsComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): SettingsComponent
    }

    fun inject(settingsFragment: SettingsFragment)
}