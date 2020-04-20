package org.stepik.android.view.injection.catalog

import dagger.Module
import dagger.Provides
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.subjects.PublishSubject
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.model.StepikFilter
import java.util.EnumSet

@Module
abstract class CatalogBusModule {
    @Module
    companion object {
        @Provides
        @JvmStatic
        @AppSingleton
        @FiltersBus
        internal fun provideFiltersBus(): PublishSubject<EnumSet<StepikFilter>> =
            PublishSubject.create()

        @Provides
        @JvmStatic
        @AppSingleton
        @FiltersBus
        internal fun provideFiltersBusObservable(
            @FiltersBus
            filtersPublisher: PublishSubject<EnumSet<StepikFilter>>,
            @BackgroundScheduler
            scheduler: Scheduler
        ): Observable<EnumSet<StepikFilter>> =
            filtersPublisher.observeOn(scheduler)
    }
}