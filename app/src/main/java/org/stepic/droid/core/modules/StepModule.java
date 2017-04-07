package org.stepic.droid.core.modules;

import android.content.Context;

import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.concurrency.MainHandler;
import org.stepic.droid.core.LessonSessionManager;
import org.stepic.droid.core.PerFragment;
import org.stepic.droid.core.VideoLengthResolver;
import org.stepic.droid.core.presenters.AnonymousPresenter;
import org.stepic.droid.core.presenters.RouteStepPresenter;
import org.stepic.droid.core.presenters.StepAttemptPresenter;
import org.stepic.droid.core.presenters.StepQualityPresenter;
import org.stepic.droid.core.presenters.StepsPresenter;
import org.stepic.droid.core.presenters.StepsTrackingPresenter;
import org.stepic.droid.core.presenters.VideoLengthPresenter;
import org.stepic.droid.core.presenters.VideoStepPresenter;
import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.preferences.UserPreferences;
import org.stepic.droid.store.operations.DatabaseFacade;
import org.stepic.droid.util.resolvers.StepTypeResolver;
import org.stepic.droid.util.resolvers.StepTypeResolverImpl;
import org.stepic.droid.util.resolvers.VideoResolver;
import org.stepic.droid.web.Api;

import java.util.concurrent.ThreadPoolExecutor;

import dagger.Module;
import dagger.Provides;

@Module
public class StepModule {

    @Provides
    @PerFragment
    VideoStepPresenter provideVideoStepPresenter(ThreadPoolExecutor threadPoolExecutor,
                                                 MainHandler mainHandler,
                                                 Api api,
                                                 DatabaseFacade databaseFacade,
                                                 VideoResolver videoResolver,
                                                 Analytic analytic) {
        return new VideoStepPresenter(threadPoolExecutor, mainHandler, api, databaseFacade, videoResolver, analytic);
    }

    @Provides
    @PerFragment
    AnonymousPresenter anonymousPresenter(SharedPreferenceHelper sharedPreferenceHelper, ThreadPoolExecutor threadPoolExecutor, MainHandler mainHandler) {
        return new AnonymousPresenter(sharedPreferenceHelper, threadPoolExecutor, mainHandler);
    }

    @Provides
    RouteStepPresenter provideNextStepPresenter(
            ThreadPoolExecutor threadPoolExecutor,
            MainHandler mainHandler,
            DatabaseFacade databaseFacade,
            Analytic analytic) {
        return new RouteStepPresenter(threadPoolExecutor, mainHandler, databaseFacade, analytic);
    }

    @Provides
    @PerFragment
    StepsPresenter provideStepPresenter(ThreadPoolExecutor threadPoolExecutor,
                                        MainHandler mainHandler,
                                        DatabaseFacade databaseFacade,
                                        Api api,
                                        SharedPreferenceHelper sharedPreferenceHelper,
                                        Analytic analytic) {
        return new StepsPresenter
                (threadPoolExecutor,
                        mainHandler,
                        databaseFacade,
                        api,
                        sharedPreferenceHelper,
                        analytic);
    }

    @Provides
    @PerFragment
    StepQualityPresenter provideStepQualityPresenter(ThreadPoolExecutor executor,
                                                     MainHandler mainHandler,
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
    StepTypeResolver provideStepResolver(Context context) {
        return new StepTypeResolverImpl(context);
    }

    @Provides
    @PerFragment
    StepAttemptPresenter provideStepAttemptProvider(MainHandler mainHandler,
                                                    ThreadPoolExecutor threadPoolExecutor,
                                                    LessonSessionManager lessonSessionManager,
                                                    Api api,
                                                    Analytic analytic,
                                                    SharedPreferenceHelper sharedPreferenceHelper) {
        return new StepAttemptPresenter(mainHandler,
                threadPoolExecutor,
                lessonSessionManager,
                api,
                analytic,
                sharedPreferenceHelper);
    }

    @Provides
    @PerFragment
    VideoLengthPresenter provideVideoLengthPresenter(MainHandler mainHandler,
                                                    ThreadPoolExecutor threadPoolExecutor,
                                                    VideoResolver videoResolver,
                                                    VideoLengthResolver videoLengthResolver) {
        return new VideoLengthPresenter(threadPoolExecutor, mainHandler, videoResolver, videoLengthResolver);
    }

    @Provides
    @PerFragment
    StepsTrackingPresenter provideStepTrackingPresenter(Analytic analytic) {
        return new StepsTrackingPresenter(analytic);
    }

}
