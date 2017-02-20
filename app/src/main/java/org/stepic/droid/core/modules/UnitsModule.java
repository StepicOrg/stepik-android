package org.stepic.droid.core.modules;

import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.concurrency.IMainHandler;
import org.stepic.droid.core.DownloadingProgressPublisher;
import org.stepic.droid.core.PerFragment;
import org.stepic.droid.core.presenters.DownloadingProgressUnitsPresenter;
import org.stepic.droid.core.presenters.UnitsPresenter;
import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.store.operations.DatabaseFacade;
import org.stepic.droid.web.IApi;

import java.util.concurrent.ThreadPoolExecutor;

import dagger.Module;
import dagger.Provides;

@Module
public class UnitsModule {

    @PerFragment
    @Provides
    public UnitsPresenter provideUnitsPresenter(Analytic analytic,
                                                ThreadPoolExecutor threadPoolExecutor,
                                                IMainHandler mainHandler,
                                                SharedPreferenceHelper sharedPreferenceHelper,
                                                DatabaseFacade databaseFacade, IApi api) {
        return new UnitsPresenter(analytic, threadPoolExecutor, mainHandler, sharedPreferenceHelper, databaseFacade, api);
    }

    @PerFragment
    @Provides
    public DownloadingProgressUnitsPresenter provideDownloadingProgressUnitsPresenter(DownloadingProgressPublisher downloadingProgressPublisher,
                                                                                      DatabaseFacade databaseFacade,
                                                                                      ThreadPoolExecutor threadPoolExecutor,
                                                                                      IMainHandler mainHandler) {
        return new DownloadingProgressUnitsPresenter(downloadingProgressPublisher, databaseFacade, threadPoolExecutor, mainHandler);
    }
}
