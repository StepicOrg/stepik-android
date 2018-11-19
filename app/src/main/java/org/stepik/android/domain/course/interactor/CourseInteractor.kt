package org.stepik.android.domain.course.interactor

import io.reactivex.Maybe
import io.reactivex.subjects.BehaviorSubject
import org.stepik.android.domain.course.model.CourseHeaderData
import org.stepik.android.domain.course.model.EnrollmentState
import org.stepik.android.domain.course.repository.CourseRepository
import org.stepik.android.domain.course.repository.CourseReviewRepository
import org.stepik.android.model.Course
import org.stepik.android.view.injection.course.CourseScope
import javax.inject.Inject

@CourseScope
class CourseInteractor
@Inject
constructor(
        private val courseRepository: CourseRepository,
        private val courseReviewRepository: CourseReviewRepository,
        private val coursePublishSubject: BehaviorSubject<Course>
) {
    fun getCourseHeaderData(courseId: Long): Maybe<CourseHeaderData> =
            courseRepository
                .getCourse(courseId)
                .flatMap(::getCourseHeaderData)

    fun getCourseHeaderData(course: Course): Maybe<CourseHeaderData> =
            courseReviewRepository
                .getCourseReview(course.reviewSummary)
                .doOnSuccess { coursePublishSubject.onNext(course) }
                .map { courseReview ->
                    CourseHeaderData(
                        courseId = course.id,
                        title = course.title ?: "",
                        cover = course.cover ?: "",
                        learnersCount = course.learnersCount,

                        review = courseReview.average,
                        progress = 0,
                        isFeatured = course.isFeatured,
                        enrollmentState = if (course.enrollment > 0) EnrollmentState.ENROLLED else EnrollmentState.NOT_ENROLLED
                    )
                }
                .toMaybe()
}