package org.stepic.droid.core.modules;

import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.concurrency.IMainHandler;
import org.stepic.droid.core.PerFragment;
import org.stepic.droid.core.ProfilePresenter;
import org.stepic.droid.core.presenters.NotificationTimePresenter;
import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.web.IApi;

import java.util.concurrent.ThreadPoolExecutor;

import dagger.Module;
import dagger.Provides;

@Module
public class ProfileModule {

    @PerFragment
    @Provides
    public ProfilePresenter provideProfilePresenter(ThreadPoolExecutor threadPoolExecutor,
                                                    Analytic analytic,
                                                    IMainHandler mainHandler,
                                                    IApi api,
                                                    SharedPreferenceHelper sharedPreferenceHelper) {
        return new ProfilePresenter(threadPoolExecutor, analytic, mainHandler, api, sharedPreferenceHelper);
    }

    @PerFragment
    @Provides
    public NotificationTimePresenter provideNotificationTimePresenter(Analytic analytic) {
        return new NotificationTimePresenter(analytic);
    }
}
