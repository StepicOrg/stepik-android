package org.stepic.droid.core.modules;

import org.stepic.droid.concurrency.IMainHandler;
import org.stepic.droid.configuration.IConfig;
import org.stepic.droid.core.IScreenManager;
import org.stepic.droid.store.operations.DatabaseFacade;
import org.stepic.droid.core.presenters.CertificatePresenter;
import org.stepic.droid.web.IApi;

import java.util.concurrent.ThreadPoolExecutor;

import dagger.Module;
import dagger.Provides;

@Module
public class CertificateModule {
    @Provides
    CertificatePresenter providePresenter(IApi api,
                                          IConfig config,
                                          IScreenManager screenManager,
                                          DatabaseFacade databaseFacade,
                                          ThreadPoolExecutor threadPoolExecutor,
                                          IMainHandler mainHandler) {
        return new CertificatePresenter (api, config, screenManager, databaseFacade, threadPoolExecutor, mainHandler);
    }
}
