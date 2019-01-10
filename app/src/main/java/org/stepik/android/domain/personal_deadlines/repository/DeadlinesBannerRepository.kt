package org.stepik.android.domain.personal_deadlines.repository

import io.reactivex.Completable
import io.reactivex.Single

interface DeadlinesBannerRepository {
    fun addCourseId(courseId: Long): Completable
    fun removeCourseId(courseId: Long): Completable
    fun hasCourseId(courseId: Long): Single<Boolean>
}