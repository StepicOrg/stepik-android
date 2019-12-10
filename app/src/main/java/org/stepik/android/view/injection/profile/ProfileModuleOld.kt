package org.stepik.android.view.injection.profile

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.profile_old.ProfilePresenter

@Module
abstract class ProfileModuleOld {
    /**
     * PRESENTATION LAYER
     */
    @Binds
    @IntoMap
    @ViewModelKey(ProfilePresenter::class)
    internal abstract fun bindProfilePresenter(profilePresenter: ProfilePresenter): ViewModel
}