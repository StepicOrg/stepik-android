package org.stepic.droid.core.modules;

import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.concurrency.MainHandler;
import org.stepic.droid.configuration.Config;
import org.stepic.droid.core.ScreenManager;
import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.storage.operations.DatabaseFacade;
import org.stepic.droid.core.presenters.CertificatePresenter;
import org.stepic.droid.web.Api;

import java.util.concurrent.ThreadPoolExecutor;

import dagger.Module;
import dagger.Provides;

@Module
public class CertificateModule {
    @Provides
    CertificatePresenter providePresenter(Api api,
                                          Config config,
                                          ScreenManager screenManager,
                                          DatabaseFacade databaseFacade,
                                          ThreadPoolExecutor threadPoolExecutor,
                                          MainHandler mainHandler,
                                          SharedPreferenceHelper sharedPreferenceHelper,
                                          Analytic analytic) {
        return new CertificatePresenter(api, config, screenManager, databaseFacade, threadPoolExecutor, mainHandler, sharedPreferenceHelper, analytic);
    }
}
