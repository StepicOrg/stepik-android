package org.stepik.android.view.injection.stories

import dagger.Subcomponent
import org.stepik.android.view.stories.ui.fragment.StoriesFragment

@Subcomponent(
    modules = [
        StoriesPresentationModule::class
    ]
)
interface StoriesComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): StoriesComponent
    }

    fun inject(storiesFragment: StoriesFragment)
}