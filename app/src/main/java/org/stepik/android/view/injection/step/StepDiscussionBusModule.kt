package org.stepik.android.view.injection.step

import dagger.Module
import dagger.Provides
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.subjects.PublishSubject
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.di.qualifiers.BackgroundScheduler

@Module
abstract class StepDiscussionBusModule {
    @Module
    companion object {
        @Provides
        @JvmStatic
        @AppSingleton
        @StepDiscussionBus
        internal fun provideStepDiscussionPublisher(): PublishSubject<Long> =
            PublishSubject.create()

        @Provides
        @JvmStatic
        @AppSingleton
        @StepDiscussionBus
        internal fun provideStepDiscussionObservable(
            @StepDiscussionBus
            stepDiscussionPublisher: PublishSubject<Long>,
            @BackgroundScheduler
            scheduler: Scheduler
        ): Observable<Long> =
            stepDiscussionPublisher.observeOn(scheduler)
    }
}