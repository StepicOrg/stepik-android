package org.stepik.android.domain.comments.repository

import io.reactivex.Completable
import io.reactivex.Single

interface CommentsBannerRepository {
    fun addCourseId(courseId: Long): Completable
    fun removeCourseId(courseId: Long): Completable
    fun hasCourseId(courseId: Long): Single<Boolean>
}