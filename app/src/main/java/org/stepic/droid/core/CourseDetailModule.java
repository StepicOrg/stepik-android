package org.stepic.droid.core;


import com.squareup.otto.Bus;

import org.stepic.droid.concurrency.IMainHandler;
import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.store.operations.DatabaseFacade;
import org.stepic.droid.ui.presenters.course_finder.CourseFinderPresenter;
import org.stepic.droid.ui.presenters.course_joiner.CourseJoinerPresenter;
import org.stepic.droid.web.IApi;

import java.util.concurrent.ThreadPoolExecutor;

import dagger.Module;
import dagger.Provides;

@Module
public class CourseDetailModule {

    @PerFragment
    @Provides
    public CourseJoinerPresenter provideCourseJoiner(
            SharedPreferenceHelper sharedPreferenceHelper,
            IApi api,
            ThreadPoolExecutor threadPoolExecutor,
            Bus bus) {
        return new CourseJoinerPresenter(sharedPreferenceHelper, api, threadPoolExecutor, bus);
    }

    @PerFragment
    @Provides
    public CourseFinderPresenter provideCourseFinderPresenter(
            ThreadPoolExecutor threadPoolExecutor,
            DatabaseFacade databaseFacade,
            IApi api,
            IMainHandler mainHandler) {
        return new CourseFinderPresenter(threadPoolExecutor, databaseFacade, api, mainHandler);
    }
}