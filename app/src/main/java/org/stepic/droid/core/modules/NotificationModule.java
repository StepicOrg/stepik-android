package org.stepic.droid.core.modules;

import com.squareup.otto.Bus;

import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.concurrency.MainHandler;
import org.stepic.droid.configuration.Config;
import org.stepic.droid.core.PerFragment;
import org.stepic.droid.core.presenters.NotificationListPresenter;
import org.stepic.droid.notifications.INotificationManager;
import org.stepic.droid.web.Api;

import java.util.concurrent.ThreadPoolExecutor;

import dagger.Module;
import dagger.Provides;

@Module
public class NotificationModule {

    @PerFragment
    @Provides
    NotificationListPresenter provideNotificationListPresenter(ThreadPoolExecutor threadPoolExecutor,
                                                               MainHandler mainHandler,
                                                               Api api,
                                                               Config config,
                                                               Bus bus,
                                                               Analytic analytic,
                                                               INotificationManager notificationManager) {
        return new NotificationListPresenter(threadPoolExecutor, mainHandler, api, config, bus, analytic, notificationManager);
    }
}
