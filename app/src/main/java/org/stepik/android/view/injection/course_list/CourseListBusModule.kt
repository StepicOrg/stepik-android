package org.stepik.android.view.injection.course_list

import dagger.Module
import dagger.Provides
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.subjects.PublishSubject
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepik.android.domain.course_list.model.UserCoursesLoaded

@Module
abstract class CourseListBusModule {
    @Module
    companion object {
        @Provides
        @JvmStatic
        @AppSingleton
        @UserCoursesLoadedBus
        internal fun provideUserCoursesLoadedPublisher(): PublishSubject<UserCoursesLoaded> =
            PublishSubject.create()

        @Provides
        @JvmStatic
        @AppSingleton
        @UserCoursesLoadedBus
        internal fun provideUserCoursesLoadedObservable(
            @UserCoursesLoadedBus
            userCoursesLoadedPublisher: PublishSubject<UserCoursesLoaded>,
            @BackgroundScheduler
            scheduler: Scheduler
        ): Observable<UserCoursesLoaded> =
            userCoursesLoadedPublisher.observeOn(scheduler)
    }
}