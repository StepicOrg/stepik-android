package org.stepic.droid.core;

import org.stepic.droid.core.presenters.NextStepPresenter;

import dagger.Module;
import dagger.Provides;

@Module
public class StepModule {
    @Provides
    public NextStepPresenter provideNextStepPresenter() {
        return new NextStepPresenter();
    }

}
