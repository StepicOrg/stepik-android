package org.stepic.droid.core.modules;

import org.stepic.droid.core.PerFragment;
import org.stepic.droid.core.presenters.NotificationListPresenter;

import dagger.Module;
import dagger.Provides;

@Module
public class NotificationModule {

    @PerFragment
    @Provides
    NotificationListPresenter provideNotificationListPresenter() {
        return new NotificationListPresenter();
    }
}
