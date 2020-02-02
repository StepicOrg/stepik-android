package org.stepik.android.view.injection.attempts

import dagger.Module
import dagger.Provides
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.subjects.PublishSubject
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.di.qualifiers.BackgroundScheduler

@Module
abstract class AttemptsBusModule {
    @Module
    companion object {
        @Provides
        @JvmStatic
        @AppSingleton
        @AttemptsBus
        internal fun provideAttemptsPublisher(): PublishSubject<Unit> =
            PublishSubject.create()

        @Provides
        @JvmStatic
        @AppSingleton
        @AttemptsBus
        internal fun provideAttemptsObservable(
            @AttemptsBus
            attemptsPublisher: PublishSubject<Unit>,
            @BackgroundScheduler
            scheduler: Scheduler
        ): Observable<Unit> =
            attemptsPublisher.observeOn(scheduler)
    }
}