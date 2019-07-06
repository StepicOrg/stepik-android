package org.stepik.android.cache.comment_banner

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.cache.comment_banner.dao.CommentBannerDao
import org.stepik.android.cache.comment_banner.structure.DbStructureCommentBanner
import org.stepik.android.data.comment_banner.source.CommentBannerCacheDataSource
import javax.inject.Inject

class CommentBannerDataCacheSourceImpl
@Inject
constructor(
    private val commentBannerDao: CommentBannerDao
) : CommentBannerCacheDataSource {
    override fun addCourseId(courseId: Long): Completable =
        Completable.fromAction {
            commentBannerDao.insertOrReplace(courseId)
        }

    override fun removeCourseId(courseId: Long): Completable =
        Completable.fromAction {
            commentBannerDao.remove(DbStructureCommentBanner.Columns.COURSE_ID, courseId.toString())
        }

    override fun hasCourseId(courseId: Long): Single<Boolean> =
        Single.fromCallable {
            commentBannerDao.isInDb(courseId)
        }
}