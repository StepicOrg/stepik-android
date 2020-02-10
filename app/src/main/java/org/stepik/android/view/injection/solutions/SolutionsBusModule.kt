package org.stepik.android.view.injection.solutions

import dagger.Module
import dagger.Provides
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.subjects.PublishSubject
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.di.qualifiers.BackgroundScheduler

@Module
abstract class SolutionsBusModule {
    @Module
    companion object {
        @Provides
        @JvmStatic
        @AppSingleton
        @SolutionsBus
        internal fun provideSolutionsPublisher(): PublishSubject<Unit> =
            PublishSubject.create()

        @Provides
        @JvmStatic
        @AppSingleton
        @SolutionsBus
        internal fun provideSolutionsObservable(
            @SolutionsBus
            solutionsPublisher: PublishSubject<Unit>,
            @BackgroundScheduler
            scheduler: Scheduler
        ): Observable<Unit> =
            solutionsPublisher.observeOn(scheduler)

        @Provides
        @JvmStatic
        @AppSingleton
        @SolutionsSentBus
        internal fun provideSolutionsSentPublisher(): PublishSubject<Unit> =
            PublishSubject.create()

        @Provides
        @JvmStatic
        @AppSingleton
        @SolutionsSentBus
        internal fun provideSolutionsSentObservable(
            @SolutionsSentBus
            solutionsSentPublisher: PublishSubject<Unit>,
            @BackgroundScheduler
            scheduler: Scheduler
        ): Observable<Unit> =
            solutionsSentPublisher.observeOn(scheduler)
    }
}