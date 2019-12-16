package org.stepic.droid.di.course_general

import dagger.Binds
import dagger.Module
import dagger.Provides
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.subjects.PublishSubject
import org.stepic.droid.base.Client
import org.stepic.droid.base.ClientImpl
import org.stepic.droid.base.ListenerContainer
import org.stepic.droid.base.ListenerContainerImpl
import org.stepic.droid.core.dropping.DroppingPosterImpl
import org.stepic.droid.core.dropping.contract.DroppingListener
import org.stepic.droid.core.dropping.contract.DroppingPoster
import org.stepic.droid.core.joining.JoiningPosterImpl
import org.stepic.droid.core.joining.contract.JoiningListener
import org.stepic.droid.core.joining.contract.JoiningPoster
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepik.android.model.Course
import org.stepik.android.view.injection.course.EnrollmentCourseUpdates

@Module
internal abstract class CourseEnrollmentBusModule {

    @Binds
    @AppSingleton
    internal abstract fun bindsClient(clientImpl: ClientImpl<DroppingListener>): Client<DroppingListener>

    @Binds
    @AppSingleton
    internal abstract fun bindContainer(listenerContainer: ListenerContainerImpl<DroppingListener>): ListenerContainer<DroppingListener>

    @Binds
    @AppSingleton
    internal abstract fun bindsPoster(posterImpl: DroppingPosterImpl): DroppingPoster


    @Binds
    @AppSingleton
    internal abstract fun bindsJoinClient(clientImpl: ClientImpl<JoiningListener>): Client<JoiningListener>

    @Binds
    @AppSingleton
    internal abstract fun bindJoinContainer(listenerContainer: ListenerContainerImpl<JoiningListener>): ListenerContainer<JoiningListener>

    @Binds
    @AppSingleton
    internal abstract fun bindsJoinPoster(posterImpl: JoiningPosterImpl): JoiningPoster

    @Module
    companion object {
        @Provides
        @JvmStatic
        @AppSingleton
        @EnrollmentCourseUpdates
        internal fun provideCourseEnrollmentSubject(): PublishSubject<Course> =
            PublishSubject.create()

        @Provides
        @JvmStatic
        @AppSingleton
        @EnrollmentCourseUpdates
        internal fun bindEnrollmentsUpdatesObservables(
            @EnrollmentCourseUpdates publisher: PublishSubject<Course>,
            @BackgroundScheduler scheduler: Scheduler
        ): Observable<Course> =
            publisher.observeOn(scheduler)
    }
}
