package org.stepik.android.presentation.course_reviews.mapper

import org.stepic.droid.util.PagedList
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course_reviews.model.CourseReviewItem
import org.stepik.android.presentation.course_reviews.CourseReviewsView
import javax.inject.Inject

class CourseReviewsStateMapper
@Inject
constructor() {
    fun isStateHasReviews(state: CourseReviewsView.State): Boolean =
        getCourseReviewItemsOfState(state)
            ?.any { it is CourseReviewItem.Data }
            ?: false

    fun getCourseReviewItemsOfState(state: CourseReviewsView.State): PagedList<CourseReviewItem>? =
        when (state) {
            is CourseReviewsView.State.CourseReviews ->
                state.courseReviewItems

            is CourseReviewsView.State.CourseReviewsLoading ->
                state.courseReviewItems

            else ->
                null
        }

    fun mergeStateWithCurrentUserReviewLoading(state: CourseReviewsView.State): CourseReviewsView.State =
        mergeStateWithCurrentUserReview(listOf(CourseReviewItem.Placeholder(isPlaceholderForCurrentUser = true)), state)

    fun mergeStateWithCurrentUserReview(currentUserReview: List<CourseReviewItem>, state: CourseReviewsView.State): CourseReviewsView.State =
        when (state) {
            is CourseReviewsView.State.EmptyContent ->
                CourseReviewsView.State.CourseReviews(
                    mergeCourseUserReview(currentUserReview, PagedList(emptyList())),
                    DataSourceType.CACHE
                )

            is CourseReviewsView.State.CourseReviews ->
                state.copy(mergeCourseUserReview(currentUserReview, state.courseReviewItems))

            is CourseReviewsView.State.CourseReviewsLoading ->
                mergeCourseUserReview(currentUserReview, state.courseReviewItems)
                    .let(CourseReviewsView.State::CourseReviewsLoading)

            else ->
                state
        }

    private fun mergeCourseUserReview(currentUserReview: List<CourseReviewItem>, reviews: PagedList<CourseReviewItem>): PagedList<CourseReviewItem> {
        val filteredReviews = reviews
            .dropWhile { courseReviewItem ->
                courseReviewItem is CourseReviewItem.Summary ||
                courseReviewItem is CourseReviewItem.ComposeBanner ||
                courseReviewItem is CourseReviewItem.Data && courseReviewItem.isCurrentUserReview ||
                courseReviewItem is CourseReviewItem.Placeholder && courseReviewItem.isPlaceholderForCurrentUser
            }

        return PagedList(currentUserReview + filteredReviews, page = reviews.page, hasPrev = reviews.hasPrev, hasNext = reviews.hasNext)
    }
}