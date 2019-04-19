package org.stepik.android.data.course_reviews.repository

import io.reactivex.Completable
import io.reactivex.Maybe
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

    override fun getCourseReviewByCourseIdAndUserId(courseId: Long, userId: Long, primarySourceType: DataSourceType): Maybe<CourseReview> {
        val remoteSource = courseReviewsRemoteDataSource
            .getCourseReviewByCourseIdAndUserId(courseId, userId)
            .doCompletableOnSuccess(courseReviewsCacheDataSource::saveCourseReview)

        val cacheSource = courseReviewsCacheDataSource
            .getCourseReviewByCourseIdAndUserId(courseId, userId)

        return when (primarySourceType) {
            DataSourceType.CACHE ->
                cacheSource.switchIfEmpty(remoteSource)

            DataSourceType.REMOTE ->
                remoteSource

            else -> throw IllegalArgumentException("Unsupported source type = $primarySourceType")
        }
    }

    override fun createCourseReview(courseReview: CourseReview): Single<CourseReview> =
        courseReviewsRemoteDataSource
            .createCourseReview(courseReview)
            .doCompletableOnSuccess(courseReviewsCacheDataSource::saveCourseReview)

    override fun saveCourseReview(courseReview: CourseReview): Single<CourseReview> =
        courseReviewsRemoteDataSource
            .saveCourseReview(courseReview)
            .doCompletableOnSuccess(courseReviewsCacheDataSource::saveCourseReview)

    override fun removeCourseReview(courseReviewId: Long): Completable =
        courseReviewsRemoteDataSource
            .removeCourseReview(courseReviewId)
            .andThen(courseReviewsCacheDataSource.removeCourseReview(courseReviewId))
}