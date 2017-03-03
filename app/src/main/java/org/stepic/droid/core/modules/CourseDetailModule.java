package org.stepic.droid.core.modules;


import com.squareup.otto.Bus;

import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.concurrency.MainHandler;
import org.stepic.droid.core.PerFragment;
import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.store.operations.DatabaseFacade;
import org.stepic.droid.core.presenters.CourseFinderPresenter;
import org.stepic.droid.core.presenters.CourseJoinerPresenter;
import org.stepic.droid.web.Api;

import java.util.concurrent.ThreadPoolExecutor;

import dagger.Module;
import dagger.Provides;

@Module
public class CourseDetailModule {

    @PerFragment
    @Provides
    CourseJoinerPresenter provideCourseJoiner(
            SharedPreferenceHelper sharedPreferenceHelper,
            Api api,
            ThreadPoolExecutor threadPoolExecutor,
            Bus bus,
            DatabaseFacade databaseFacade,
            Analytic analytic) {
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
}