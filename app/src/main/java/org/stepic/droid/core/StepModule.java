package org.stepic.droid.core;

import org.stepic.droid.concurrency.IMainHandler;
import org.stepic.droid.core.presenters.NextStepPresenter;
import org.stepic.droid.store.operations.DatabaseFacade;

import java.util.concurrent.ThreadPoolExecutor;

import dagger.Module;
import dagger.Provides;

@Module
public class StepModule {
    @Provides
    public NextStepPresenter provideNextStepPresenter(
            ThreadPoolExecutor threadPoolExecutor,
            IMainHandler mainHandler,
            DatabaseFacade databaseFacade) {
        return new NextStepPresenter(threadPoolExecutor, mainHandler, databaseFacade);
    }

}
