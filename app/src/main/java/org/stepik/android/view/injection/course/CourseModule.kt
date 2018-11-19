package org.stepik.android.view.injection.course

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.stepic.droid.di.qualifiers.CourseId
import org.stepik.android.cache.course.source.CourseCacheDataSourceImpl
import org.stepik.android.cache.course.source.EnrollmentCacheDataSourceImpl
import org.stepik.android.data.course.repository.CourseRepositoryImpl
import org.stepik.android.data.course.repository.CourseReviewRepositoryImpl
import org.stepik.android.data.course.repository.EnrollmentRepositoryImpl
import org.stepik.android.data.course.source.*
import org.stepik.android.domain.course.model.EnrollmentState
import org.stepik.android.domain.course.repository.CourseRepository
import org.stepik.android.domain.course.repository.CourseReviewRepository
import org.stepik.android.domain.course.repository.EnrollmentRepository
import org.stepik.android.model.Course
import org.stepik.android.presentation.base.injection.DaggerViewModelFactory
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.course.CoursePresenter
import org.stepik.android.remote.course.source.CourseRemoteDataSourceImpl
import org.stepik.android.remote.course.source.CourseReviewRemoteDataSourceImpl
import org.stepik.android.remote.course.source.EnrollmentRemoteDataSourceImpl

@Module
abstract class CourseModule {
    /**
     * DATA LAYER
     */
    @Binds
    internal abstract fun bindCourseRepository(
        courseRepositoryImpl: CourseRepositoryImpl): CourseRepository

    @Binds
    internal abstract fun bindCourseRemoteDataSource(
        courseRemoteDataSourceImpl: CourseRemoteDataSourceImpl): CourseRemoteDataSource

    @Binds
    internal abstract fun bindCourseCacheDataSource(
        courseCacheDataSourceImpl: CourseCacheDataSourceImpl): CourseCacheDataSource

    @Binds
    internal abstract fun bindEnrollmentRepository(
        enrollmentRepositoryImpl: EnrollmentRepositoryImpl): EnrollmentRepository

    @Binds
    internal abstract fun bindEnrollmentRemoteDataSource(
        enrollmentRemoteDataSourceImpl: EnrollmentRemoteDataSourceImpl): EnrollmentRemoteDataSource

    @Binds
    internal abstract fun bindEnrollmentCacheDataSource(
        enrollmentCacheDataSourceImpl: EnrollmentCacheDataSourceImpl): EnrollmentCacheDataSource

    @Binds
    internal abstract fun bindCourseReviewRepository(
        courseReviewRepositoryImpl: CourseReviewRepositoryImpl): CourseReviewRepository

    @Binds
    internal abstract fun bindCourseReviewRemoteDataSource(
        courseReviewRemoteDataSourceImpl: CourseReviewRemoteDataSourceImpl): CourseReviewRemoteDataSource

    /**
     * PRESENTATION LAYER
     */
    @Binds
    @IntoMap
    @ViewModelKey(CoursePresenter::class)
    internal abstract fun bindCoursePresenter(coursePresenter: CoursePresenter): ViewModel

    @Binds
    @CourseScope
    internal abstract fun bindViewModelFactory(daggerViewModelFactory: DaggerViewModelFactory): ViewModelProvider.Factory

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
        internal fun provideCourseEnrollmentSubject(): PublishSubject<Pair<Long, EnrollmentState>> =
            PublishSubject.create()
    }
}