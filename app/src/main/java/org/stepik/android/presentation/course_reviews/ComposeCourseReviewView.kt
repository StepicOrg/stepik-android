package org.stepik.android.presentation.course_reviews

import org.stepik.android.domain.course_reviews.model.CourseReview

interface ComposeCourseReviewView {
    sealed class State {
        object Idle : State()
        object Loading : State()
        class Complete(val courseReview: CourseReview) : State()
    }

    fun setState(state: State)
    fun showNetworkError()
}