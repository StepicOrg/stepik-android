package org.stepic.droid.di.downloads

import dagger.Subcomponent
import org.stepic.droid.ui.fragments.DownloadsFragment

@DownloadsScope
@Subcomponent
interface DownloadsComponent {

    @Subcomponent.Builder
    interface Builder {
        fun build(): DownloadsComponent
    }

    fun inject(fragment: DownloadsFragment)
}
