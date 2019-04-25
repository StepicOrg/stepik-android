package org.stepic.droid.di.downloads

import dagger.Subcomponent
import org.stepic.droid.ui.fragments.DownloadsFragment
import org.stepik.android.view.injection.step.StepDataModule

@DownloadsScope
@Subcomponent(modules = [
    StepDataModule::class
])
interface DownloadsComponent {

    @Subcomponent.Builder
    interface Builder {
        fun build(): DownloadsComponent
    }

    fun inject(fragment: DownloadsFragment)
}
