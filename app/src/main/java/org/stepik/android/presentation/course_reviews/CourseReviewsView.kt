package org.stepik.android.presentation.course_reviews

import org.stepic.droid.util.PagedList
import org.stepik.android.domain.course_reviews.model.CourseReviewItem

interface CourseReviewsView {
    sealed class State {
        object Idle : State()
        object Loading : State()
        object EmptyContent : State()
        object NetworkError : State()

        class CourseReviewsCache(val courseReviewItems: PagedList<CourseReviewItem>) : State()
        class CourseReviewsRemote(val courseReviewItems: PagedList<CourseReviewItem>) : State()
    }

    fun setState(state: State)
    fun showNetworkError()
}