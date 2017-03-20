package org.stepic.droid.core.modules;

import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.concurrency.MainHandler;
import org.stepic.droid.core.PerFragment;
import org.stepic.droid.core.presenters.LoginPresenter;
import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.web.Api;

import java.util.concurrent.ThreadPoolExecutor;

import dagger.Module;
import dagger.Provides;

@Module
public class LoginModule {

    @Provides
    @PerFragment
    LoginPresenter provideLoginPresenter(Api api,
                                         Analytic analytic,
                                         ThreadPoolExecutor threadPoolExecutor,
                                         SharedPreferenceHelper sharedPreferenceHelper,
                                         MainHandler mainHandler) {
        return new LoginPresenter(api, analytic, sharedPreferenceHelper, threadPoolExecutor, mainHandler);
    }
}
