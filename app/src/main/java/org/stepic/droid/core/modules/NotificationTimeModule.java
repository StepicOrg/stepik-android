package org.stepic.droid.core.modules;

import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.concurrency.IMainHandler;
import org.stepic.droid.core.PerFragment;
import org.stepic.droid.core.presenters.NotificationTimePresenter;
import org.stepic.droid.notifications.LocalReminder;
import org.stepic.droid.preferences.SharedPreferenceHelper;

import java.util.concurrent.ThreadPoolExecutor;

import dagger.Module;
import dagger.Provides;

@Module
public class NotificationTimeModule {
    @PerFragment
    @Provides
    public NotificationTimePresenter provideNotificationTimePresenter(Analytic analytic,
                                                                      ThreadPoolExecutor threadPoolExecutor,
                                                                      IMainHandler mainHandler,
                                                                      SharedPreferenceHelper sharedPreferenceHelper,
                                                                      LocalReminder localReminder) {
        return new NotificationTimePresenter(analytic, threadPoolExecutor, mainHandler, sharedPreferenceHelper, localReminder);
    }
}
