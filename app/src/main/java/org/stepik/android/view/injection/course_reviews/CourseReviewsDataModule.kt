package org.stepik.android.view.injection.course_reviews

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.cache.course_reviews.CourseReviewsCacheDataSourceImpl
import org.stepik.android.data.course_reviews.repository.CourseReviewsRepositoryImpl
import org.stepik.android.data.course_reviews.source.CourseReviewsCacheDataSource
import org.stepik.android.data.course_reviews.source.CourseReviewsRemoteDataSource
import org.stepik.android.domain.course_reviews.repository.CourseReviewsRepository
import org.stepik.android.remote.course_reviews.CourseReviewsRemoteDataSourceImpl
import org.stepik.android.remote.course_reviews.service.CourseReviewService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit

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

    @Module
    companion object {
        @Provides
        @JvmStatic
        internal fun provideCourseReviewService(@Authorized retrofit: Retrofit): CourseReviewService =
            retrofit.create(CourseReviewService::class.java)
    }
}