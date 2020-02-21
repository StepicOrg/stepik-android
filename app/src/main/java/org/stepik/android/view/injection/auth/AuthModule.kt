package org.stepik.android.view.injection.auth

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.auth.LaunchPresenter
import org.stepik.android.presentation.base.injection.ViewModelKey

@Module
internal abstract class AuthModule {

    /**
     * PRESENTATION LAYER
     */
    @Binds
    @IntoMap
    @ViewModelKey(LaunchPresenter::class)
    internal abstract fun bindLaunchPresenter(launchPresenter: LaunchPresenter): ViewModel
}