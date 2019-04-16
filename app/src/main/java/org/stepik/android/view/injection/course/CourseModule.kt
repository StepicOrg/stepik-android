package org.stepik.android.view.injection.course

import android.arch.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepik.android.data.course.repository.CourseReviewRepositoryImpl
import org.stepik.android.data.course.source.CourseReviewRemoteDataSource
import org.stepik.android.domain.course.repository.CourseReviewRepository
import org.stepik.android.model.Course
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.course.CoursePresenter
import org.stepik.android.presentation.course_content.CourseContentPresenter
import org.stepik.android.presentation.course_info.CourseInfoPresenter
import org.stepik.android.presentation.course_reviews.CourseReviewsPresenter
import org.stepik.android.remote.course.source.CourseReviewRemoteDataSourceImpl

@Module
abstract class CourseModule {
    /**
     * PRESENTATION LAYER
     */
    @Binds
    @IntoMap
    @ViewModelKey(CoursePresenter::class)
    internal abstract fun bindCoursePresenter(coursePresenter: CoursePresenter): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CourseInfoPresenter::class)
    internal abstract fun bindCourseInfoPresenter(courseInfoPresenter: CourseInfoPresenter): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CourseReviewsPresenter::class)
    internal abstract fun bindCourseReviewsPrsenter(courseReviewsPresenter: CourseReviewsPresenter): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CourseContentPresenter::class)
    internal abstract fun bindCourseContentPrsenter(courseContentPresenter: CourseContentPresenter): ViewModel

    @Module
    companion object {
        @Provides
        @JvmStatic
        @CourseScope
        internal fun provideCourseBehaviorSubject(): BehaviorSubject<Course> =
            BehaviorSubject.create()

        @Provides
        @JvmStatic
        @CourseScope
        internal fun provideCourseObservableSource(courseSubject: BehaviorSubject<Course>, @BackgroundScheduler scheduler: Scheduler): Observable<Course> =
            courseSubject.observeOn(scheduler)

        @Provides
        @JvmStatic
        @CourseScope
        @EnrollmentCourseUpdates
        internal fun provideCourseEnrollmentSubject(): PublishSubject<Course> =
            PublishSubject.create()

        @Provides
        @JvmStatic
        @CourseScope
        @EnrollmentCourseUpdates
        internal fun bindEnrollmentsUpdatesObservables(
            @EnrollmentCourseUpdates publisher: PublishSubject<Course>,
            @BackgroundScheduler scheduler: Scheduler
        ): Observable<Course> =
            publisher.observeOn(scheduler)
    }
}