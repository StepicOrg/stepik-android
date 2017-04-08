package org.stepic.droid.di.streak;

import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.core.presenters.StreakPresenter;
import org.stepic.droid.notifications.LocalReminder;
import org.stepic.droid.preferences.SharedPreferenceHelper;

import dagger.Module;
import dagger.Provides;

@Module
public abstract class StreakModule {
    @Provides
    static StreakPresenter provideStreakPresenter(Analytic analytic,
                                                  SharedPreferenceHelper sharedPreferenceHelper,
                                                  LocalReminder localReminder) {
        return new StreakPresenter(analytic, sharedPreferenceHelper, localReminder);
    }
}
