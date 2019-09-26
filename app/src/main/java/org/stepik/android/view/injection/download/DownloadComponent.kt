package org.stepik.android.view.injection.download

import dagger.Subcomponent
import org.stepik.android.view.download.ui.DownloadActivity

@Subcomponent(modules = [
    DownloadModule::class
])
interface DownloadComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): DownloadComponent
    }

    fun inject(downloadActivity: DownloadActivity)
}