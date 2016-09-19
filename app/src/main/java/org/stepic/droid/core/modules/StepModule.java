package org.stepic.droid.core.modules;

import android.content.Context;

import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.concurrency.IMainHandler;
import org.stepic.droid.core.PerFragment;
import org.stepic.droid.core.presenters.RouteStepPresenter;
import org.stepic.droid.core.presenters.StepQualityPresenter;
import org.stepic.droid.core.presenters.StepsPresenter;
import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.preferences.UserPreferences;
import org.stepic.droid.store.operations.DatabaseFacade;
import org.stepic.droid.util.resolvers.StepTypeResolver;
import org.stepic.droid.util.resolvers.StepTypeResolverImpl;
import org.stepic.droid.web.IApi;

import java.util.concurrent.ThreadPoolExecutor;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class StepModule {
    @Provides
    RouteStepPresenter provideNextStepPresenter(
            ThreadPoolExecutor threadPoolExecutor,
            IMainHandler mainHandler,
            DatabaseFacade databaseFacade,
            Analytic analytic) {
        return new RouteStepPresenter(threadPoolExecutor, mainHandler, databaseFacade, analytic);
    }

    @Provides
    @PerFragment
    StepsPresenter provideStepPresenter(ThreadPoolExecutor threadPoolExecutor,
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
    StepQualityPresenter provideStepQualityPresenter(ThreadPoolExecutor executor,
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


    @Provides
    @Singleton
    StepTypeResolver provideStepResolver(Context context) {
        return new StepTypeResolverImpl(context);
    }
}
