package org.stepik.android.view.injection.progress

import dagger.Module
import dagger.Provides
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.subjects.PublishSubject
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepik.android.model.Progress

@Module
abstract class ProgressBusModule {
    @Module
    companion object {
        @Provides
        @JvmStatic
        @AppSingleton
        internal fun provideProgressPublisher(): PublishSubject<Progress> =
            PublishSubject.create()

        @Provides
        @JvmStatic
        @AppSingleton
        internal fun provideProgressObservable(progressPublisher: PublishSubject<Progress>, @BackgroundScheduler scheduler: Scheduler): Observable<Progress> =
            progressPublisher.observeOn(scheduler)
    }
}