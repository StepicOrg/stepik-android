package org.stepik.android.cache.course_reviews

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.storage.dao.IDao
import org.stepic.droid.util.PagedList
import org.stepik.android.cache.course_reviews.structure.DbStructureCourseReview
import org.stepik.android.data.course_reviews.source.CourseReviewsCacheDataSource
import org.stepik.android.domain.course_reviews.model.CourseReview
import javax.inject.Inject

class CourseReviewsCacheDataSourceImpl
@Inject
constructor(
    private val courseReviewsDao: IDao<CourseReview>
) : CourseReviewsCacheDataSource {
    companion object {
        private const val PAGE_SIZE = 20
    }

    override fun getCourseReviewsByCourseId(courseId: Long): Single<PagedList<CourseReview>> =
        Single.fromCallable {
            PagedList(
                courseReviewsDao
                    .getAllWithQuery(
                        """
                            SELECT * FROM ${DbStructureCourseReview.TABLE_NAME}
                            WHERE ${DbStructureCourseReview.Columns.COURSE} = ?
                            ORDER BY ${DbStructureCourseReview.Columns.ID} DESC
                            LIMIT ?
                        """.trimIndent(),
                        arrayOf(courseId.toString(), PAGE_SIZE.toString())
                    )
            )
        }

    override fun saveCourseReviews(courseReviews: List<CourseReview>): Completable =
        Completable.fromAction {
            courseReviewsDao.insertOrReplaceAll(courseReviews)
        }

}