package org.stepik.android.view.injection.download

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import org.stepik.android.view.injection.base.ViewModelFactoryModule
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.download.DownloadPresenter

@Module(includes = [ViewModelFactoryModule::class])
abstract class DownloadModule {
    /**
     * PRESENTATION LAYER
     */
    @Binds
    @IntoMap
    @ViewModelKey(DownloadPresenter::class)
    internal abstract fun bindDownloadPresenter(downloadPresenter: DownloadPresenter): ViewModel
}