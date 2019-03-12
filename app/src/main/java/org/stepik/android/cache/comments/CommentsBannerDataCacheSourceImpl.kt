package org.stepik.android.cache.comments

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.cache.comments.dao.CommentsBannerDao
import org.stepik.android.cache.comments.structure.DbStructureCommentsBanner
import org.stepik.android.data.comments.source.CommentsBannerCacheDataSource
import javax.inject.Inject

class CommentsBannerDataCacheSourceImpl
@Inject
constructor(
    private val commentsBannerDao: CommentsBannerDao
) : CommentsBannerCacheDataSource {
    override fun addCourseId(courseId: Long): Completable =
        Completable.fromAction {
            commentsBannerDao.insertOrReplace(courseId)
        }

    override fun removeCourseId(courseId: Long): Completable =
        Completable.fromAction {
            commentsBannerDao.remove(DbStructureCommentsBanner.Columns.COURSE_ID, courseId.toString())
        }

    override fun hasCourseId(courseId: Long): Single<Boolean> =
        Single.fromCallable {
            commentsBannerDao.isInDb(courseId)
        }
}