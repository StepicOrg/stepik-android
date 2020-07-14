package org.stepik.android.view.injection.course

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.cache.course.source.CourseCacheDataSourceImpl
import org.stepik.android.cache.course.source.CourseReviewSummaryCacheDataSourceImpl
import org.stepik.android.cache.course.source.EnrollmentCacheDataSourceImpl
import org.stepik.android.cache.course_list.CourseListQueryCacheDataSourceImpl
import org.stepik.android.data.course.repository.CourseRepositoryImpl
import org.stepik.android.data.course.repository.CourseReviewSummaryRepositoryImpl
import org.stepik.android.data.course.repository.EnrollmentRepositoryImpl
import org.stepik.android.data.course.source.CourseCacheDataSource
import org.stepik.android.data.course.source.CourseRemoteDataSource
import org.stepik.android.data.course.source.CourseReviewSummaryCacheDataSource
import org.stepik.android.data.course.source.CourseReviewSummaryRemoteDataSource
import org.stepik.android.data.course.source.EnrollmentCacheDataSource
import org.stepik.android.data.course.source.EnrollmentRemoteDataSource
import org.stepik.android.data.course_list.source.CourseListQueryCacheDataSource
import org.stepik.android.domain.course.repository.CourseRepository
import org.stepik.android.domain.course.repository.CourseReviewSummaryRepository
import org.stepik.android.domain.course.repository.EnrollmentRepository
import org.stepik.android.remote.course.service.CourseReviewSummaryService
import org.stepik.android.remote.course.service.CourseService
import org.stepik.android.remote.course.service.EnrollmentService
import org.stepik.android.remote.course.source.CourseRemoteDataSourceImpl
import org.stepik.android.remote.course.source.CourseReviewSummaryRemoteDataSourceImpl
import org.stepik.android.remote.course.source.EnrollmentRemoteDataSourceImpl
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit

@Module
abstract class CourseDataModule {
    @Binds
    internal abstract fun bindCourseRepository(
        courseRepositoryImpl: CourseRepositoryImpl
    ): CourseRepository

    @Binds
    internal abstract fun bindCourseRemoteDataSource(
        courseRemoteDataSourceImpl: CourseRemoteDataSourceImpl
    ): CourseRemoteDataSource

    @Binds
    internal abstract fun bindCourseCacheDataSource(
        courseCacheDataSourceImpl: CourseCacheDataSourceImpl
    ): CourseCacheDataSource

    @Binds
    internal abstract fun bindCourseListQueryCacheDataSource(
        courseListQueryCacheDataSourceImpl: CourseListQueryCacheDataSourceImpl
    ): CourseListQueryCacheDataSource

    @Binds
    internal abstract fun bindEnrollmentRepository(
        enrollmentRepositoryImpl: EnrollmentRepositoryImpl
    ): EnrollmentRepository

    @Binds
    internal abstract fun bindEnrollmentRemoteDataSource(
        enrollmentRemoteDataSourceImpl: EnrollmentRemoteDataSourceImpl
    ): EnrollmentRemoteDataSource

    @Binds
    internal abstract fun bindEnrollmentCacheDataSource(
        enrollmentCacheDataSourceImpl: EnrollmentCacheDataSourceImpl
    ): EnrollmentCacheDataSource

    @Binds
    internal abstract fun bindCourseReviewRepository(
        courseReviewRepositoryImpl: CourseReviewSummaryRepositoryImpl
    ): CourseReviewSummaryRepository

    @Binds
    internal abstract fun bindCourseReviewRemoteDataSource(
        courseReviewRemoteDataSourceImpl: CourseReviewSummaryRemoteDataSourceImpl
    ): CourseReviewSummaryRemoteDataSource

    @Binds
    internal abstract fun bindCourseReviewCacheDataSource(
        courseReviewSummaryCacheDataSourceImpl: CourseReviewSummaryCacheDataSourceImpl
    ): CourseReviewSummaryCacheDataSource

    @Module
    companion object {
        @Provides
        @JvmStatic
        internal fun provideCourseService(@Authorized retrofit: Retrofit): CourseService =
            retrofit.create(CourseService::class.java)

        @Provides
        @JvmStatic
        internal fun provideEnrollmentService(@Authorized retrofit: Retrofit): EnrollmentService =
            retrofit.create(EnrollmentService::class.java)

        @Provides
        @JvmStatic
        internal fun provideCourseReviewSummaryService(@Authorized retrofit: Retrofit): CourseReviewSummaryService =
            retrofit.create(CourseReviewSummaryService::class.java)
    }
}