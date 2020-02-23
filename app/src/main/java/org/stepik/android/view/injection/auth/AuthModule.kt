package org.stepik.android.view.injection.auth

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.auth.CredentialAuthPresenter
import org.stepik.android.presentation.auth.RegistrationPresenter
import org.stepik.android.presentation.auth.SocialAuthPresenter
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

    @Binds
    @IntoMap
    @ViewModelKey(SocialAuthPresenter::class)
    internal abstract fun bindSocialAuthPresenter(socialAuthPresenter: SocialAuthPresenter): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RegistrationPresenter::class)
    internal abstract fun bindRegistrationPresenter(registrationPresenter: RegistrationPresenter): ViewModel
}