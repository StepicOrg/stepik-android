package org.stepik.android.cache.personal_deadlines

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.cache.personal_deadlines.dao.DeadlinesBannerDao
import org.stepik.android.cache.personal_deadlines.structure.DbStructureDeadlinesBanner
import org.stepik.android.data.personal_deadlines.source.DeadlinesBannerCacheDataSource
import javax.inject.Inject

class DeadlinesBannerCacheDataSourceImpl
@Inject
constructor(
    private val deadlinesBannerDao: DeadlinesBannerDao
) : DeadlinesBannerCacheDataSource {

    override fun addCourseId(courseId: Long): Completable =
        Completable.fromAction {
            deadlinesBannerDao.insertOrReplace(courseId)
        }

    override fun removeCourseId(courseId: Long): Completable =
        Completable.fromAction {
            deadlinesBannerDao.remove(DbStructureDeadlinesBanner.Columns.COURSE_ID, courseId.toString())
        }

    override fun hasCourseId(courseId: Long): Single<Boolean> =
        Single.fromCallable {
            deadlinesBannerDao.isInDb(courseId)
        }
}