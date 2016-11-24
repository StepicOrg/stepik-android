package org.stepic.droid.core.modules;

import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.core.PerFragment;
import org.stepic.droid.core.presenters.NotificationTimePresenter;

import dagger.Module;
import dagger.Provides;

@Module
public class NotificationTimeModule {
    @PerFragment
    @Provides
    public NotificationTimePresenter provideNotificationTimePresenter(Analytic analytic) {
        return new NotificationTimePresenter(analytic);
    }
}
