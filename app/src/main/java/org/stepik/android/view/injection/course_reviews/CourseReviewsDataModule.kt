package org.stepik.android.view.injection.course_reviews

import dagger.Binds
import dagger.Module
import org.stepik.android.cache.course_reviews.CourseReviewsCacheDataSourceImpl
import org.stepik.android.data.course_reviews.repository.CourseReviewsRepositoryImpl
import org.stepik.android.data.course_reviews.source.CourseReviewsCacheDataSource
import org.stepik.android.data.course_reviews.source.CourseReviewsRemoteDataSource
import org.stepik.android.domain.course_reviews.repository.CourseReviewsRepository
import org.stepik.android.remote.course_reviews.CourseReviewsRemoteDataSourceImpl

@Module
abstract class CourseReviewsDataModule {
    @Binds
    internal abstract fun bindCourseReviewsRepository(
        courseReviewsRepositoryImpl: CourseReviewsRepositoryImpl
    ): CourseReviewsRepository

    @Binds
    internal abstract fun bindCourseReviewsRemoteDataSource(
        courseReviewsRemoteDataSourceImpl: CourseReviewsRemoteDataSourceImpl
    ): CourseReviewsRemoteDataSource

    @Binds
    internal abstract fun bindCourseReviewsCacheDataStore(
        courseReviewsCacheDataSourceImpl: CourseReviewsCacheDataSourceImpl
    ): CourseReviewsCacheDataSource
}