package org.stepik.android.view.injection.step

import com.jakewharton.rxrelay3.BehaviorRelay
import dagger.Module
import dagger.Provides
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.persistence.model.StepPersistentWrapper

@Module
abstract class StepWrapperBusModule {
    @Module
    companion object {
        @Provides
        @JvmStatic
        @AppSingleton
        @StepWrapperBus
        internal fun provideBehaviorRelay(stepWrapper: StepPersistentWrapper): BehaviorRelay<StepPersistentWrapper> =
            BehaviorRelay.createDefault(stepWrapper)
    }
}