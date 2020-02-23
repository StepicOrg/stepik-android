package org.stepik.android.view.injection.auth

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.auth.CredentialAuthPresenter
import org.stepik.android.presentation.base.injection.ViewModelKey

@Module
internal abstract class AuthModule {

    /**
     * PRESENTATION LAYER
     */
    @Binds
    @IntoMap
    @ViewModelKey(CredentialAuthPresenter::class)
    internal abstract fun bindCredentialAuthPresenter(credentialAuthPresenter: CredentialAuthPresenter): ViewModel
}