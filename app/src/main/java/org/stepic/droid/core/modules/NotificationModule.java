package org.stepic.droid.core.modules;

import org.stepic.droid.concurrency.IMainHandler;
import org.stepic.droid.configuration.IConfig;
import org.stepic.droid.core.PerFragment;
import org.stepic.droid.core.presenters.NotificationListPresenter;
import org.stepic.droid.web.IApi;

import java.util.concurrent.ThreadPoolExecutor;

import dagger.Module;
import dagger.Provides;

@Module
public class NotificationModule {

    @PerFragment
    @Provides
    NotificationListPresenter provideNotificationListPresenter(ThreadPoolExecutor threadPoolExecutor,
                                                               IMainHandler mainHandler,
                                                               IApi api,
                                                               IConfig config) {
        return new NotificationListPresenter(threadPoolExecutor, mainHandler, api, config);
    }
}
