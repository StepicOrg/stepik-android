package org.stepik.android.data.comments.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.data.comments.source.CommentsBannerCacheDataSource
import org.stepik.android.domain.comments.repository.CommentsBannerRepository
import javax.inject.Inject

class CommentsBannerRepositoryImpl
@Inject
constructor(
    private val commentsBannerCacheDataSource: CommentsBannerCacheDataSource
) : CommentsBannerRepository {
    override fun addCourseId(courseId: Long): Completable =
        commentsBannerCacheDataSource.addCourseId(courseId)

    override fun removeCourseId(courseId: Long): Completable =
        commentsBannerCacheDataSource.removeCourseId(courseId)

    override fun hasCourseId(courseId: Long): Single<Boolean> =
        commentsBannerCacheDataSource.hasCourseId(courseId)
}