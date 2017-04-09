package org.stepic.droid.di.mainscreen

import dagger.Binds
import dagger.Module
import org.stepic.droid.core.ProfilePresenter
import org.stepic.droid.core.presenters.ProfilePresenterImpl

@Module
interface MainScreenModule {

    @Binds
    @MainScreenScope
    fun provideProfilePresenter(profilePresenter: ProfilePresenterImpl): ProfilePresenter
}
