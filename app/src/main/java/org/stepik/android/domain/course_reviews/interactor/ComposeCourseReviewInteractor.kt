package org.stepik.android.domain.course_reviews.interactor

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.domain.course_reviews.model.CourseReview
import org.stepik.android.domain.course_reviews.repository.CourseReviewsRepository
import javax.inject.Inject

class ComposeCourseReviewInteractor
@Inject
constructor(
    private val courseReviewsRepository: CourseReviewsRepository
) {

    fun createCourseReview(courseReview: CourseReview): Single<CourseReview> =
        courseReviewsRepository
            .createCourseReview(courseReview)

    fun updateCourseReview(courseReview: CourseReview): Single<CourseReview> =
        courseReviewsRepository
            .saveCourseReview(courseReview)

    fun removeCourseReview(courseReviewId: Long): Completable =
        courseReviewsRepository
            .removeCourseReview(courseReviewId)
}