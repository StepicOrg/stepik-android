package org.stepik.android.view.injection.step_content_text

import dagger.Subcomponent
import org.stepik.android.view.step_content_text.ui.fragment.TextStepContentFragment

@Subcomponent(modules = [
    TextStepContentModule::class
])
interface TextStepContentComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): TextStepContentComponent
    }

    fun inject(textStepContentFragment: TextStepContentFragment)
}