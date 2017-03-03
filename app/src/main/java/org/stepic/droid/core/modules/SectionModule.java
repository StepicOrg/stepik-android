package org.stepic.droid.core.modules;


import android.app.DownloadManager;
import android.content.Context;

import com.squareup.otto.Bus;

import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.concurrency.MainHandler;
import org.stepic.droid.configuration.Config;
import org.stepic.droid.core.DownloadingProgressSectionPublisher;
import org.stepic.droid.core.PerFragment;
import org.stepic.droid.core.presenters.CalendarPresenter;
import org.stepic.droid.core.presenters.CourseFinderPresenter;
import org.stepic.droid.core.presenters.CourseJoinerPresenter;
import org.stepic.droid.core.presenters.DownloadingProgressSectionsPresenter;
import org.stepic.droid.core.presenters.InvitationPresenter;
import org.stepic.droid.core.presenters.SectionsPresenter;
import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.preferences.UserPreferences;
import org.stepic.droid.store.CancelSniffer;
import org.stepic.droid.store.operations.DatabaseFacade;
import org.stepic.droid.web.Api;

import java.util.concurrent.ThreadPoolExecutor;

import dagger.Module;
import dagger.Provides;

@Module
public class SectionModule {

    @PerFragment
    @Provides
    CourseJoinerPresenter provideCourseJoiner(
            SharedPreferenceHelper sharedPreferenceHelper,
            Api api,
            ThreadPoolExecutor threadPoolExecutor,
            Bus bus,
            DatabaseFacade databaseFacade, Analytic analytic) {
        return new CourseJoinerPresenter(sharedPreferenceHelper, api, threadPoolExecutor, bus, databaseFacade, analytic);
    }

    @PerFragment
    @Provides
    CourseFinderPresenter provideCourseFinderPresenter(
            ThreadPoolExecutor threadPoolExecutor,
            DatabaseFacade databaseFacade,
            Api api,
            MainHandler mainHandler) {
        return new CourseFinderPresenter(threadPoolExecutor, databaseFacade, api, mainHandler);
    }

    @PerFragment
    @Provides
    CalendarPresenter provideCalendarPresenter(
            Config config,
            MainHandler mainHandler,
            Context context,
            ThreadPoolExecutor threadPoolExecutor,
            DatabaseFacade databaseFacade,
            UserPreferences userPreferences,
            Analytic analytic) {
        return new CalendarPresenter(config, mainHandler, context, threadPoolExecutor, databaseFacade, userPreferences, analytic);
    }

    @PerFragment
    @Provides
    SectionsPresenter provideSectionsPresenter(ThreadPoolExecutor threadPoolExecutor,
                                               MainHandler mainHandler,
                                               Api api,
                                               DatabaseFacade databaseFacade) {
        return new SectionsPresenter(
                threadPoolExecutor,
                mainHandler,
                api,
                databaseFacade);
    }

    @PerFragment
    @Provides
    InvitationPresenter provideInvitationPresenter(ThreadPoolExecutor threadPoolExecutor,
                                                   MainHandler mainHandler,
                                                   SharedPreferenceHelper sharedPreferenceHelper,
                                                   Analytic analytic) {
        return new InvitationPresenter(threadPoolExecutor, mainHandler, sharedPreferenceHelper, analytic);
    }

    @PerFragment
    @Provides
    DownloadingProgressSectionsPresenter provideDownloadingProgressPresenter(DownloadingProgressSectionPublisher downloadingProgressSectionPublisher) {
        return new DownloadingProgressSectionsPresenter(downloadingProgressSectionPublisher);
    }


    @Provides
    @PerFragment
    DownloadingProgressSectionPublisher progressPublisher(DatabaseFacade databaseFacade, DownloadManager downloadManager, CancelSniffer cancelSniffer, MainHandler mainHandler) {
        return new DownloadingProgressSectionPublisher(databaseFacade, downloadManager, cancelSniffer, mainHandler);
    }

}
