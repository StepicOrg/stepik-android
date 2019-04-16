package org.stepik.android.data.course_reviews.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.util.PagedList
import org.stepic.droid.util.doCompletableOnSuccess
import org.stepik.android.data.course_reviews.source.CourseReviewsCacheDataSource
import org.stepik.android.data.course_reviews.source.CourseReviewsRemoteDataSource
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course_reviews.model.CourseReview
import org.stepik.android.domain.course_reviews.repository.CourseReviewsRepository
import javax.inject.Inject

class CourseReviewsRepositoryImpl
@Inject
constructor(
    private val courseReviewsCacheDataSource: CourseReviewsCacheDataSource,
    private val courseReviewsRemoteDataSource: CourseReviewsRemoteDataSource
) : CourseReviewsRepository {
    override fun getCourseReviewsByCourseId(courseId: Long, page: Int, sourceType: DataSourceType): Single<PagedList<CourseReview>> =
        when (sourceType) {
            DataSourceType.CACHE ->
                courseReviewsCacheDataSource
                    .getCourseReviewsByCourseId(courseId)

            DataSourceType.REMOTE ->
                courseReviewsRemoteDataSource
                    .getCourseReviewsByCourseId(courseId, page)
                    .doCompletableOnSuccess(courseReviewsCacheDataSource::saveCourseReviews)

            else -> throw IllegalArgumentException("Unsupported source type = $sourceType")
        }

    override fun createCourseReview(courseReview: CourseReview): Completable =
        courseReviewsRemoteDataSource
            .createCourseReview(courseReview)
            .doCompletableOnSuccess(courseReviewsCacheDataSource::saveCourseReview)
            .ignoreElement()

    override fun saveCourseReview(courseReview: CourseReview): Completable =
        courseReviewsRemoteDataSource
            .saveCourseReview(courseReview)
            .doCompletableOnSuccess(courseReviewsCacheDataSource::saveCourseReview)
            .ignoreElement()

    override fun removeCourseReview(courseReviewId: Long): Completable =
        courseReviewsRemoteDataSource
            .removeCourseReview(courseReviewId)
            .andThen(courseReviewsCacheDataSource.removeCourseReview(courseReviewId))
}