package org.stepik.android.domain.comment_banner.repository

import io.reactivex.Completable
import io.reactivex.Single

interface CommentBannerRepository {
    fun addCourseId(courseId: Long): Completable
    fun removeCourseId(courseId: Long): Completable
    fun hasCourseId(courseId: Long): Single<Boolean>
}