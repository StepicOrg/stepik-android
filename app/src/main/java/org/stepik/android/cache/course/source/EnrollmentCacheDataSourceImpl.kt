package org.stepik.android.cache.course.source

import io.reactivex.Completable
import org.stepik.android.data.course.source.EnrollmentCacheDataSource
import javax.inject.Inject

class EnrollmentCacheDataSourceImpl
@Inject
constructor() : EnrollmentCacheDataSource {
    override fun addEnrollment(courseId: Long): Completable =
        Completable.complete() // TODO

    override fun removeEnrollment(courseId: Long): Completable =
        Completable.complete() // TODO
}