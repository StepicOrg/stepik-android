package org.stepik.android.view.injection.font_size_settings

import dagger.Subcomponent
import org.stepik.android.view.font_size_settings.ui.dialog.ChooseFontSizeDialogFragment

@Subcomponent(modules = [
    FontSizeModule::class
])
interface FontSizeComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): FontSizeComponent
    }

    fun inject(chooseFontSizeDialogFragment: ChooseFontSizeDialogFragment)
}