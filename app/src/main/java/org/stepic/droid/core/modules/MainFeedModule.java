package org.stepic.droid.core.modules;

import android.app.DownloadManager;

import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.concurrency.IMainHandler;
import org.stepic.droid.core.presenters.ProfileMainFeedPresenter;
import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.preferences.UserPreferences;
import org.stepic.droid.store.operations.DatabaseFacade;
import org.stepic.droid.web.IApi;

import java.util.concurrent.ThreadPoolExecutor;

import dagger.Module;
import dagger.Provides;

@Module
public class MainFeedModule {

    @Provides
    ProfileMainFeedPresenter provideProfileMainFeedPresenter(IMainHandler mainHandler,
                                                             ThreadPoolExecutor threadPoolExecutor,
                                                             Analytic analytic,
                                                             SharedPreferenceHelper sharedPreferenceHelper,
                                                             IApi api,
                                                             DatabaseFacade databaseFacade,
                                                             DownloadManager systemDownloadManager,
                                                             UserPreferences userPreferences) {
        return new ProfileMainFeedPresenter(sharedPreferenceHelper, mainHandler, api, threadPoolExecutor, analytic, databaseFacade, userPreferences, systemDownloadManager);
    }
}
