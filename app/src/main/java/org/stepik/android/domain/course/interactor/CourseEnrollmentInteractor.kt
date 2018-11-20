package org.stepik.android.domain.course.interactor

import io.reactivex.Completable
import io.reactivex.subjects.PublishSubject
import org.stepik.android.domain.course.model.EnrollmentState
import org.stepik.android.domain.course.repository.EnrollmentRepository
import org.stepik.android.view.injection.course.EnrollmentCourseUpdates
import javax.inject.Inject

class CourseEnrollmentInteractor
@Inject
constructor(
    private val enrollmentRepository: EnrollmentRepository,
    @EnrollmentCourseUpdates
    private val enrollmentSubject: PublishSubject<Long>
) {
    fun enrollCourse(courseId: Long): Completable =
        enrollmentRepository
            .addEnrollment(courseId)
            .doOnComplete { enrollmentSubject.onNext(courseId) } // notify everyone about changes

    fun dropCourse(courseId: Long): Completable =
        enrollmentRepository
            .removeEnrollment(courseId)
            .doOnComplete { enrollmentSubject.onNext(courseId) } // notify everyone about changes
}