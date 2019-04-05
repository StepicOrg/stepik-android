package org.stepik.android.data.comments.source

import io.reactivex.Completable
import io.reactivex.Single

interface CommentsBannerCacheDataSource {
    fun addCourseId(courseId: Long): Completable
    fun removeCourseId(courseId: Long): Completable
    fun hasCourseId(courseId: Long): Single<Boolean>
}