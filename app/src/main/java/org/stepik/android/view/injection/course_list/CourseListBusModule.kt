package org.stepik.android.view.injection.course_list

import dagger.Module
import dagger.Provides
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.subjects.PublishSubject
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepik.android.domain.course_list.model.UserCoursesLoaded
import org.stepik.android.domain.user_courses.model.UserCourse
import org.stepik.android.model.Course

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

        @Provides
        @JvmStatic
        @AppSingleton
        @UserCoursesUpdateBus
        internal fun provideUserCoursesUpdatePublisher(): PublishSubject<Course> =
            PublishSubject.create()

        @Provides
        @JvmStatic
        @AppSingleton
        @UserCoursesUpdateBus
        internal fun provideUserCoursesUpdateObservable(
            @UserCoursesUpdateBus
            userCoursesUpdatePublisher: PublishSubject<Course>,
            @BackgroundScheduler
            scheduler: Scheduler
        ): Observable<Course> =
            userCoursesUpdatePublisher.observeOn(scheduler)

        @Provides
        @JvmStatic
        @AppSingleton
        @UserCoursesOperationBus
        internal fun provideUserCoursesOperationPublisher(): PublishSubject<UserCourse> =
            PublishSubject.create()

        @Provides
        @JvmStatic
        @AppSingleton
        @UserCoursesOperationBus
        internal fun provideUserCoursesOperationObservable(
            @UserCoursesOperationBus
            userCoursesOperationPublisher: PublishSubject<UserCourse>,
            @BackgroundScheduler
            scheduler: Scheduler
        ): Observable<UserCourse> =
            userCoursesOperationPublisher.observeOn(scheduler)
    }
}