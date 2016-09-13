package org.stepic.droid.core;

import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.concurrency.IMainHandler;
import org.stepic.droid.core.presenters.RouteStepPresenter;
import org.stepic.droid.core.presenters.StepQualityPresenter;
import org.stepic.droid.core.presenters.StepsPresenter;
import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.preferences.UserPreferences;
import org.stepic.droid.store.operations.DatabaseFacade;
import org.stepic.droid.web.IApi;

import java.util.concurrent.ThreadPoolExecutor;

import dagger.Module;
import dagger.Provides;

@Module
public class StepModule {
    @Provides
    public RouteStepPresenter provideNextStepPresenter(
            ThreadPoolExecutor threadPoolExecutor,
            IMainHandler mainHandler,
            DatabaseFacade databaseFacade,
            Analytic analytic) {
        return new RouteStepPresenter(threadPoolExecutor, mainHandler, databaseFacade, analytic);
    }

    @Provides
    @PerFragment
    public StepsPresenter provideStepPresenter(ThreadPoolExecutor threadPoolExecutor,
                                               IMainHandler mainHandler,
                                               DatabaseFacade databaseFacade,
                                               IApi api,
                                               SharedPreferenceHelper sharedPreferenceHelper) {
        return new StepsPresenter
                (threadPoolExecutor,
                        mainHandler,
                        databaseFacade,
                        api,
                        sharedPreferenceHelper);
    }

    @Provides
    @PerFragment
    public StepQualityPresenter provideStepQualityPresenter(ThreadPoolExecutor executor,
                                                            IMainHandler mainHandler,
                                                            DatabaseFacade databaseFacade,
                                                            UserPreferences userPreferences,
                                                            Analytic analytic) {
        return new StepQualityPresenter(executor,
                mainHandler,
                databaseFacade,
                userPreferences,
                analytic);
    }
}
