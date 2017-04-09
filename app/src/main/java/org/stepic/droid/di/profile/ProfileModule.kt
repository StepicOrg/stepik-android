package org.stepic.droid.di.profile

import dagger.Binds
import dagger.Module
import org.stepic.droid.core.ProfilePresenter
import org.stepic.droid.core.presenters.ProfilePresenterImpl


@Module
interface ProfileModule {

    @Binds
    @ProfileScope
    fun provideProfilePresenter(profilePresenter: ProfilePresenterImpl): ProfilePresenter
}
