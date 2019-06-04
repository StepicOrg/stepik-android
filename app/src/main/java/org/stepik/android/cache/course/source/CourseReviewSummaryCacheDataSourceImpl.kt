package org.stepik.android.cache.course.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.storage.dao.IDao
import org.stepik.android.data.course.source.CourseReviewSummaryCacheDataSource
import org.stepik.android.model.CourseReviewSummary
import javax.inject.Inject

class CourseReviewSummaryCacheDataSourceImpl
@Inject
constructor(
    private val courseReviewSummaryDao: IDao<CourseReviewSummary>
) : CourseReviewSummaryCacheDataSource {
    override fun getCourseReviewSummaries(vararg courseReviewSummaryIds: Long): Single<List<CourseReviewSummary>> =
        Single.fromCallable {
            courseReviewSummaryDao.getAll()
        }

    override fun saveCourseReviewSummaries(courseReviewSummaries: List<CourseReviewSummary>): Completable =
        Completable.fromAction {
            courseReviewSummaryDao.insertOrReplaceAll(courseReviewSummaries)
        }
}