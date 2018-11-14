package org.stepik.android.domain.course.interactor

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.stepik.android.domain.course.repository.CourseRepository
import org.stepik.android.domain.course.repository.EnrollmentRepository
import org.stepik.android.model.Course
import org.stepik.android.view.injection.course.CourseScope
import javax.inject.Inject

@CourseScope
class CourseInteractor
@Inject
constructor(
        private val courseRepository: CourseRepository,
        private val enrollmentRepository: EnrollmentRepository,

        private val coursePublishSubject: BehaviorSubject<Course>,

        private val enrollmentSubject: PublishSubject<Pair<Long, Boolean>>
) {
    fun getCourse(courseId: Long): Maybe<Course> =
            courseRepository
                    .getCourse(courseId) // get course from api
                    .doOnSuccess(::notifyCourse) // send loaded course to ancestors

    fun notifyCourse(course: Course) =
            coursePublishSubject.onNext(course)

    fun enrollCourse(courseId: Long): Completable =
            enrollmentRepository
                    .addEnrollment(courseId)
                    .doOnComplete { enrollmentSubject.onNext(courseId to true) } // notify everyone about changes

    fun dropCourse(courseId: Long): Completable =
            enrollmentRepository
                    .removeEnrollment(courseId)
                    .doOnComplete { enrollmentSubject.onNext(courseId to false) } // notify everyone about changes

}