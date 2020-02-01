package org.stepik.android.presentation.course_reviews.mapper

import org.stepic.droid.util.PagedList
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
            is CourseReviewsView.State.CourseReviewsCache ->
                state.courseReviewItems

            is CourseReviewsView.State.CourseReviewsRemote ->
                state.courseReviewItems

            is CourseReviewsView.State.CourseReviewsRemoteLoading ->
                state.courseReviewItems

            else ->
                null
        }

    fun mergeStateWithCurrentUserReviewLoading(state: CourseReviewsView.State): CourseReviewsView.State =
        mergeStateWithCurrentUserReview(listOf(CourseReviewItem.Placeholder(isPlaceholderForCurrentUser = true)), state)

    fun mergeStateWithCurrentUserReview(currentUserReview: List<CourseReviewItem>, state: CourseReviewsView.State): CourseReviewsView.State =
        when (state) {
            is CourseReviewsView.State.EmptyContent ->
                mergeCourseUserReview(currentUserReview, PagedList(emptyList()))
                    .let(CourseReviewsView.State::CourseReviewsCache)

            is CourseReviewsView.State.CourseReviewsCache ->
                mergeCourseUserReview(currentUserReview, state.courseReviewItems)
                    .let(CourseReviewsView.State::CourseReviewsCache)

            is CourseReviewsView.State.CourseReviewsRemote ->
                mergeCourseUserReview(currentUserReview, state.courseReviewItems)
                    .let(CourseReviewsView.State::CourseReviewsRemote)

            is CourseReviewsView.State.CourseReviewsRemoteLoading ->
                mergeCourseUserReview(currentUserReview, state.courseReviewItems)
                    .let(CourseReviewsView.State::CourseReviewsRemoteLoading)

            else ->
                state
        }

    private fun mergeCourseUserReview(currentUserReview: List<CourseReviewItem>, reviews: PagedList<CourseReviewItem>): PagedList<CourseReviewItem> {
        val summary = reviews.takeWhile { it is CourseReviewItem.Summary }

        val filteredReviews = reviews
            .dropWhile { courseReviewItem ->
                courseReviewItem is CourseReviewItem.Summary ||
                courseReviewItem is CourseReviewItem.ComposeBanner ||
                courseReviewItem is CourseReviewItem.Data && courseReviewItem.isCurrentUserReview ||
                courseReviewItem is CourseReviewItem.Placeholder && courseReviewItem.isPlaceholderForCurrentUser
            }

        return PagedList(summary + currentUserReview + filteredReviews, page = reviews.page, hasPrev = reviews.hasPrev, hasNext = reviews.hasNext)
    }
}