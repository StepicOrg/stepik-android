package org.stepic.droid.di.home

import dagger.Module
import dagger.Provides
import io.reactivex.Scheduler
import org.stepic.droid.core.presenters.HomeStreakPresenter
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.web.Api

@Module
class HomeModule {
    @Provides
    @HomeScope
    internal fun provideStreakPresenter(@BackgroundScheduler backgroundScheduler: Scheduler,
                                        @MainScheduler mainScheduler: Scheduler,
                                        api: Api,
                                        sharedPreferences: SharedPreferenceHelper): HomeStreakPresenter =
         HomeStreakPresenter(backgroundScheduler, mainScheduler, api, sharedPreferences)
}