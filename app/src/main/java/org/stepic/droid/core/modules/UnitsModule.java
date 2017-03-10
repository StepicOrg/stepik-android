package org.stepic.droid.core.modules;

import android.app.DownloadManager;

import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.concurrency.MainHandler;
import org.stepic.droid.core.DownloadingProgressUnitPublisher;
import org.stepic.droid.core.PerFragment;
import org.stepic.droid.core.presenters.DownloadingProgressUnitsPresenter;
import org.stepic.droid.core.presenters.UnitsPresenter;
import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.store.CancelSniffer;
import org.stepic.droid.store.operations.DatabaseFacade;
import org.stepic.droid.web.Api;

import java.util.concurrent.ThreadPoolExecutor;

import dagger.Module;
import dagger.Provides;

@Module
public class UnitsModule {

    @PerFragment
    @Provides
    public UnitsPresenter provideUnitsPresenter(Analytic analytic,
                                                ThreadPoolExecutor threadPoolExecutor,
                                                MainHandler mainHandler,
                                                SharedPreferenceHelper sharedPreferenceHelper,
                                                DatabaseFacade databaseFacade, Api api) {
        return new UnitsPresenter(analytic, threadPoolExecutor, mainHandler, sharedPreferenceHelper, databaseFacade, api);
    }

    @Provides
    @PerFragment
    DownloadingProgressUnitPublisher progressPublisher(DatabaseFacade databaseFacade, DownloadManager downloadManager, CancelSniffer cancelSniffer, MainHandler mainHandler) {
        return new DownloadingProgressUnitPublisher(databaseFacade, downloadManager, cancelSniffer, mainHandler);
    }


    @PerFragment
    @Provides
    public DownloadingProgressUnitsPresenter provideDownloadingProgressUnitsPresenter(DownloadingProgressUnitPublisher downloadingProgressUnitPublisher) {
        return new DownloadingProgressUnitsPresenter(downloadingProgressUnitPublisher);
    }
}
