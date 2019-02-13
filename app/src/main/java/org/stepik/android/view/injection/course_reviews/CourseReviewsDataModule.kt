package org.stepik.android.view.injection.course_reviews

import dagger.Binds
import dagger.Module
import org.stepik.android.data.course_reviews.repository.CourseReviewsRepositoryImpl
import org.stepik.android.data.course_reviews.source.CourseReviewsRemoteDataSource
import org.stepik.android.domain.course_reviews.repository.CourseReviewsRepository
import org.stepik.android.remote.course_reviews.CourseReviewsRemoteDataSourceImpl

@Module
abstract class CourseReviewsDataModule {

    @Binds
    internal abstract fun bindCoursePaymentsRepository(
        courseReviewsRepositoryImpl: CourseReviewsRepositoryImpl
    ): CourseReviewsRepository

    @Binds
    internal abstract fun bindCoursePaymentsRemoteDataSource(
        courseReviewsRemoteDataSourceImpl: CourseReviewsRemoteDataSourceImpl
    ): CourseReviewsRemoteDataSource

}