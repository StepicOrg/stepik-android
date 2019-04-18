package org.stepik.android.view.injection.course

import dagger.Binds
import dagger.Module
import org.stepik.android.cache.course.source.CourseCacheDataSourceImpl
import org.stepik.android.cache.course.source.EnrollmentCacheDataSourceImpl
import org.stepik.android.data.course.repository.CourseRepositoryImpl
import org.stepik.android.data.course.repository.CourseReviewRepositoryImpl
import org.stepik.android.data.course.repository.EnrollmentRepositoryImpl
import org.stepik.android.data.course.source.CourseCacheDataSource
import org.stepik.android.data.course.source.CourseRemoteDataSource
import org.stepik.android.data.course.source.CourseReviewRemoteDataSource
import org.stepik.android.data.course.source.EnrollmentCacheDataSource
import org.stepik.android.data.course.source.EnrollmentRemoteDataSource
import org.stepik.android.domain.course.repository.CourseRepository
import org.stepik.android.domain.course.repository.CourseReviewRepository
import org.stepik.android.domain.course.repository.EnrollmentRepository
import org.stepik.android.remote.course.source.CourseRemoteDataSourceImpl
import org.stepik.android.remote.course.source.CourseReviewRemoteDataSourceImpl
import org.stepik.android.remote.course.source.EnrollmentRemoteDataSourceImpl

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
        courseReviewRepositoryImpl: CourseReviewRepositoryImpl
    ): CourseReviewRepository

    @Binds
    internal abstract fun bindCourseReviewRemoteDataSource(
        courseReviewRemoteDataSourceImpl: CourseReviewRemoteDataSourceImpl
    ): CourseReviewRemoteDataSource
}