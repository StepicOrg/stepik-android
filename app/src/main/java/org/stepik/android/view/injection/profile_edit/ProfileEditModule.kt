package org.stepik.android.view.injection.profile_edit

import android.arch.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.profile_edit.ProfileEditInfoPresenter
import org.stepik.android.presentation.profile_edit.ProfileEditPasswordPresenter
import org.stepik.android.presentation.profile_edit.ProfileEditPresenter

@Module
abstract class ProfileEditModule {

    /**
     * Presentation layer
     */
    @Binds
    @IntoMap
    @ViewModelKey(ProfileEditInfoPresenter::class)
    internal abstract fun bindProfileEditInfoPresenter(profileEditInfoPresenter: ProfileEditInfoPresenter): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ProfileEditPasswordPresenter::class)
    internal abstract fun bindProfileEditPasswordPresenter(profileEditPasswordPresenter: ProfileEditPasswordPresenter): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ProfileEditPresenter::class)
    internal abstract fun bindProfileEditPresenter(profileEditPresenter: ProfileEditPresenter): ViewModel
}