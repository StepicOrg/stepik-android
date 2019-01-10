package org.stepik.android.domain.course.repository

import io.reactivex.Completable

interface EnrollmentRepository {
    fun addEnrollment(courseId: Long): Completable
    fun removeEnrollment(courseId: Long): Completable
}