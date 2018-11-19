package org.stepik.android.domain.course.interactor

import io.reactivex.Completable
import io.reactivex.subjects.PublishSubject
import org.stepik.android.domain.course.repository.EnrollmentRepository
import javax.inject.Inject

class CourseEnrollmentInteractor
@Inject
constructor(
    private val enrollmentRepository: EnrollmentRepository,
    private val enrollmentSubject: PublishSubject<Pair<Long, Boolean>>
) {
    fun enrollCourse(courseId: Long): Completable =
        enrollmentRepository
            .addEnrollment(courseId)
            .doOnComplete { enrollmentSubject.onNext(courseId to true) } // notify everyone about changes

    fun dropCourse(courseId: Long): Completable =
        enrollmentRepository
            .removeEnrollment(courseId)
            .doOnComplete { enrollmentSubject.onNext(courseId to false) } // notify everyone about changes
}