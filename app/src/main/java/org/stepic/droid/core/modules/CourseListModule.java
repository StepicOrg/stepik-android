package org.stepic.droid.core.modules;

import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.concurrency.MainHandler;
import org.stepic.droid.core.FilterApplicator;
import org.stepic.droid.core.PerFragment;
import org.stepic.droid.core.presenters.ContinueCoursePresenter;
import org.stepic.droid.core.presenters.PersistentCourseListPresenter;
import org.stepic.droid.core.presenters.SearchCoursesPresenter;
import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.store.operations.DatabaseFacade;
import org.stepic.droid.util.resolvers.SearchResolver;
import org.stepic.droid.util.resolvers.SearchResolverImpl;
import org.stepic.droid.web.IApi;

import java.util.concurrent.ThreadPoolExecutor;

import dagger.Module;
import dagger.Provides;

@Module
public class CourseListModule {

    @PerFragment
    @Provides
    SearchResolver provideSearchResolver() {
        return new SearchResolverImpl();
    }

    @PerFragment
    @Provides
    PersistentCourseListPresenter providePersistentCourseListPresenter(Analytic analytic,
                                                                       DatabaseFacade databaseFacade,
                                                                       ThreadPoolExecutor threadPoolExecutor,
                                                                       MainHandler mainHandler,
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
    SearchCoursesPresenter provideSearchCoursePresenter(IApi api,
                                                        ThreadPoolExecutor threadPoolExecutor,
                                                        MainHandler mainHandler,
                                                        SearchResolver searchResolver) {
        return new SearchCoursesPresenter(api,
                threadPoolExecutor,
                mainHandler,
                searchResolver);
    }


    @Provides
    @PerFragment
    ContinueCoursePresenter provideContinueCoursePresenter(DatabaseFacade databaseFacade,
                                                           ThreadPoolExecutor threadPoolExecutor,
                                                           MainHandler mainHandler,
                                                           IApi api) {
        return new ContinueCoursePresenter(databaseFacade, api, threadPoolExecutor, mainHandler);
    }
}
