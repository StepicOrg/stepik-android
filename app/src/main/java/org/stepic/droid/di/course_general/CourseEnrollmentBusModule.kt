package org.stepic.droid.di.course_general

import dagger.Module
import dagger.Provides
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.subjects.PublishSubject
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepik.android.model.Course
import org.stepik.android.view.injection.course.EnrollmentCourseUpdates

@Module
internal abstract class CourseEnrollmentBusModule {

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
