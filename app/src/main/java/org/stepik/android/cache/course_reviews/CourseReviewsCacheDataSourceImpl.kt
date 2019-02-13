package org.stepik.android.cache.course_reviews

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.storage.dao.IDao
import org.stepik.android.cache.course_reviews.structure.DbStructureCourseReview
import org.stepik.android.data.course_reviews.source.CourseReviewsCacheDataSource
import org.stepik.android.domain.course_reviews.model.CourseReview
import javax.inject.Inject

class CourseReviewsCacheDataSourceImpl
@Inject
constructor(
    private val courseReviewsDao: IDao<CourseReview>
) : CourseReviewsCacheDataSource {

    override fun getCourseReviewsByCourseId(courseId: Long): Single<List<CourseReview>> =
        Single.fromCallable {
            courseReviewsDao.getAll(DbStructureCourseReview.Columns.COURSE, courseId.toString())
        }

    override fun saveCourseReviews(courseReviews: List<CourseReview>): Completable =
        Completable.fromAction {
            courseReviewsDao.insertOrReplaceAll(courseReviews)
        }

}