package org.stepic.droid.di.streak

import dagger.Module
import dagger.Provides
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.core.presenters.StreakPresenter
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepik.android.view.notification.delegate.StreakNotificationDelegate

@Module
class StreakModule {
    @Provides
    internal fun provideStreakPresenter(analytic: Analytic,
                                        sharedPreferenceHelper: SharedPreferenceHelper,
                                        streakNotificationDelegate: StreakNotificationDelegate): StreakPresenter {
        return StreakPresenter(analytic, sharedPreferenceHelper, streakNotificationDelegate)
    }
}
