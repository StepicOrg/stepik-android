package org.stepik.android.presentation.course_reviews

import org.stepik.android.domain.course_reviews.model.CourseReviewItem

interface CourseReviewsView {
    sealed class State {
        object Idle : State()
        object Loading : State()
        object EmptyContent : State()
        object NetworkError : State()

        class CourseReviewsLoaded(val courseReviewItems: List<CourseReviewItem>) : State()
    }

    fun setState(state: State)
}