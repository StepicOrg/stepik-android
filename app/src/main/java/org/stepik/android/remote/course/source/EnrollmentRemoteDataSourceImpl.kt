package org.stepik.android.remote.course.source

import io.reactivex.Completable
import org.stepic.droid.web.Api
import org.stepik.android.data.course.source.EnrollmentRemoteDataSource
import javax.inject.Inject

class EnrollmentRemoteDataSourceImpl
@Inject
constructor(
    private val api: Api
) : EnrollmentRemoteDataSource {
    override fun addEnrollment(courseId: Long): Completable =
        api.joinCourse(courseId)

    override fun removeEnrollment(courseId: Long): Completable =
        api.dropCourse(courseId)
}