package org.stepic.droid.core;

import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.concurrency.IMainHandler;
import org.stepic.droid.core.presenters.PersistentCourseListPresenter;
import org.stepic.droid.core.presenters.SearchCoursesPresenter;
import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.store.operations.DatabaseFacade;
import org.stepic.droid.util.resolvers.ISearchResolver;
import org.stepic.droid.web.IApi;

import java.util.concurrent.ThreadPoolExecutor;

import dagger.Module;
import dagger.Provides;

@Module
public class CourseListModule {
    @PerFragment
    @Provides
    public PersistentCourseListPresenter providePersistentCourseListPresenter(Analytic analytic,
                                                                              DatabaseFacade databaseFacade,
                                                                              ThreadPoolExecutor threadPoolExecutor,
                                                                              IMainHandler mainHandler,
                                                                              IApi api,
                                                                              FilterApplicator filterApplicator,
                                                                              SharedPreferenceHelper sharedPreferenceHelper) {
        return new PersistentCourseListPresenter(
                analytic,
                databaseFacade,
                threadPoolExecutor,
                mainHandler,
                api,
                filterApplicator,
                sharedPreferenceHelper);
    }


    @PerFragment
    @Provides
    public SearchCoursesPresenter provideSearchCoursePresenter(IApi api,
                                                               ThreadPoolExecutor threadPoolExecutor,
                                                               IMainHandler mainHandler,
                                                               ISearchResolver searchResolver) {
        return new SearchCoursesPresenter(api,
                threadPoolExecutor,
                mainHandler,
                searchResolver);
    }
}
