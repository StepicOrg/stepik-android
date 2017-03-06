package org.stepic.droid.core.modules;

import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.concurrency.MainHandler;
import org.stepic.droid.core.PerFragment;
import org.stepic.droid.core.ProfilePresenter;
import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.web.Api;

import java.util.concurrent.ThreadPoolExecutor;

import dagger.Module;
import dagger.Provides;

@Module
public class ProfileModule {

    @PerFragment
    @Provides
    public ProfilePresenter provideProfilePresenter(ThreadPoolExecutor threadPoolExecutor,
                                                    Analytic analytic,
                                                    MainHandler mainHandler,
                                                    Api api,
                                                    SharedPreferenceHelper sharedPreferenceHelper) {
        return new ProfilePresenter(threadPoolExecutor, analytic, mainHandler, api, sharedPreferenceHelper);
    }
}
