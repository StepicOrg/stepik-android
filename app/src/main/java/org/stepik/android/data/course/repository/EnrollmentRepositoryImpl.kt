package org.stepik.android.data.course.repository

import io.reactivex.Completable
import org.stepic.droid.util.then
import org.stepik.android.data.course.source.EnrollmentCacheDataSource
import org.stepik.android.data.course.source.EnrollmentRemoteDataSource
import org.stepik.android.domain.course.repository.EnrollmentRepository
import javax.inject.Inject

class EnrollmentRepositoryImpl
@Inject
constructor(
    private val enrollmentCacheDataSource: EnrollmentCacheDataSource,
    private val enrollmentRemoteDataSource: EnrollmentRemoteDataSource
) : EnrollmentRepository {
    override fun addEnrollment(courseId: Long): Completable =
        enrollmentRemoteDataSource.addEnrollment(courseId) then
                enrollmentCacheDataSource.addEnrollment(courseId)

    override fun removeEnrollment(courseId: Long): Completable =
        enrollmentRemoteDataSource.removeEnrollment(courseId) then
                enrollmentCacheDataSource.removeEnrollment(courseId)
}