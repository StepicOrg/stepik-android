package org.stepik.android.view.injection.step_quiz

import dagger.Module
import dagger.Provides
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.subjects.PublishSubject
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepik.android.domain.code_preference.model.InitCodePreference

@Module
abstract class StepQuizBusModule {
    @Module
    companion object {
        @Provides
        @JvmStatic
        @AppSingleton
        @StepQuizBus
        internal fun provideStepQuizPublisher(): PublishSubject<Long> =
            PublishSubject.create()

        @Provides
        @JvmStatic
        @AppSingleton
        @StepQuizBus
        internal fun provideStepQuizObservable(
            @StepQuizBus
            stepQuizPublisher: PublishSubject<Long>,
            @BackgroundScheduler
            scheduler: Scheduler
        ): Observable<Long> =
            stepQuizPublisher.observeOn(scheduler)

        @Provides
        @JvmStatic
        @AppSingleton
        @CodePreferenceBus
        internal fun provideCodePreferencePublisher(): PublishSubject<InitCodePreference> =
            PublishSubject.create()

        @Provides
        @JvmStatic
        @AppSingleton
        @CodePreferenceBus
        internal fun provideCodePreferenceObservable(
            @CodePreferenceBus
            codePreferencePublisher: PublishSubject<InitCodePreference>,
            @BackgroundScheduler
            scheduler: Scheduler
        ): Observable<InitCodePreference> =
            codePreferencePublisher.observeOn(scheduler)
    }
}