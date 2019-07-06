package org.stepik.android.data.comment_banner.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.data.comment_banner.source.CommentBannerCacheDataSource
import org.stepik.android.domain.comment_banner.repository.CommentBannerRepository
import javax.inject.Inject

class CommentBannerRepositoryImpl
@Inject
constructor(
    private val commentsBannerCacheDataSource: CommentBannerCacheDataSource
) : CommentBannerRepository {
    override fun addCourseId(courseId: Long): Completable =
        commentsBannerCacheDataSource.addCourseId(courseId)

    override fun removeCourseId(courseId: Long): Completable =
        commentsBannerCacheDataSource.removeCourseId(courseId)

    override fun hasCourseId(courseId: Long): Single<Boolean> =
        commentsBannerCacheDataSource.hasCourseId(courseId)
}