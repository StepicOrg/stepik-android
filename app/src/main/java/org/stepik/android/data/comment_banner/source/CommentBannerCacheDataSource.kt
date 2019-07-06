package org.stepik.android.data.comment_banner.source

import io.reactivex.Completable
import io.reactivex.Single

interface CommentBannerCacheDataSource {
    fun addCourseId(courseId: Long): Completable
    fun removeCourseId(courseId: Long): Completable
    fun hasCourseId(courseId: Long): Single<Boolean>
}