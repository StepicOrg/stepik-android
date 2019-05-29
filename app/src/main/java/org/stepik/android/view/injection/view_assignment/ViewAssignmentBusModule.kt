package org.stepik.android.view.injection.view_assignment

import dagger.Module
import dagger.Provides
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.subjects.BehaviorSubject
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.di.qualifiers.BackgroundScheduler

@Module
abstract class ViewAssignmentBusModule {
    @Module
    companion object {
        /**
         * Provides publisher that should be notified if view report was failed to post
         */
        @Provides
        @JvmStatic
        @AppSingleton
        @ViewAssignmentBus
        internal fun provideViewAssignmentPublisher(): BehaviorSubject<Unit> =
            BehaviorSubject.createDefault(Unit)

        @Provides
        @JvmStatic
        @AppSingleton
        @ViewAssignmentBus
        internal fun provideViewAssignmentObservable(
            @ViewAssignmentBus
            vewAssignmentPublisher: BehaviorSubject<Unit>,

            @BackgroundScheduler
            scheduler: Scheduler
        ): Observable<Unit> =
            vewAssignmentPublisher.observeOn(scheduler)
    }
}