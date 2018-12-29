package org.stepik.android.data.personal_deadlines.source

import io.reactivex.Completable
import io.reactivex.Single

interface DeadlinesBannerCacheDataSource {
    fun addCourseId(courseId: Long): Completable
    fun removeCourseId(courseId: Long): Completable
    fun hasCourseId(courseId: Long): Single<Boolean>
}