package org.stepik.android.remote.course.source

import io.reactivex.Completable
import org.stepic.droid.web.Api
import org.stepik.android.data.course.source.EnrollmentRemoteDataSource
import org.stepik.android.model.Enrollment
import org.stepik.android.remote.course.model.EnrollmentRequest
import javax.inject.Inject

class EnrollmentRemoteDataSourceImpl
@Inject
constructor(
    private val api: Api
) : EnrollmentRemoteDataSource {
    override fun addEnrollment(courseId: Long): Completable =
        api.joinCourse(EnrollmentRequest(Enrollment(courseId)))

    override fun removeEnrollment(courseId: Long): Completable =
        api.dropCourse(courseId)
}