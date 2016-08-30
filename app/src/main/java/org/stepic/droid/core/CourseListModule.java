package org.stepic.droid.core;

import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.concurrency.IMainHandler;
import org.stepic.droid.core.presenters.FilterForCoursesPresenter;
import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.store.operations.DatabaseFacade;

import java.util.concurrent.ThreadPoolExecutor;

import dagger.Module;
import dagger.Provides;

@Module
public class CourseListModule {
    @PerFragment
    @Provides
    public FilterForCoursesPresenter provideFilterForCoursePresenter(SharedPreferenceHelper sharedPreferenceHelper,
                                                                     Analytic analytic,
                                                                     ThreadPoolExecutor executor,
                                                                     IMainHandler mainHandler,
                                                                     DatabaseFacade databaseFacade) {
        return new FilterForCoursesPresenter
                (sharedPreferenceHelper,
                        analytic,
                        executor,
                        mainHandler,
                        databaseFacade);
    }
}
