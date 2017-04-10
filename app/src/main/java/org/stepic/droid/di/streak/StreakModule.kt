package org.stepic.droid.di.streak

import dagger.Module
import dagger.Provides
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.core.presenters.StreakPresenter
import org.stepic.droid.notifications.LocalReminder
import org.stepic.droid.preferences.SharedPreferenceHelper

@Module
class StreakModule {
    @Provides
    internal fun provideStreakPresenter(analytic: Analytic,
                                        sharedPreferenceHelper: SharedPreferenceHelper,
                                        localReminder: LocalReminder): StreakPresenter {
        return StreakPresenter(analytic, sharedPreferenceHelper, localReminder)
    }
}
