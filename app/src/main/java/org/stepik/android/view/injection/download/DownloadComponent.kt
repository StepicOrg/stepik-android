package org.stepik.android.view.injection.download

import dagger.Subcomponent
import org.stepik.android.view.download.ui.activity.DownloadActivity
import org.stepik.android.view.injection.course.CourseDataModule

@Subcomponent(modules = [
    DownloadModule::class,
    DownloadDataModule::class,
    CourseDataModule::class
])
interface DownloadComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): DownloadComponent
    }

    fun inject(downloadActivity: DownloadActivity)
}