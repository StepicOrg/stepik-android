package org.stepik.android.presentation.course_reviews.mapper

import org.stepic.droid.util.PagedList
import org.stepik.android.domain.course_reviews.model.CourseReviewItem
import org.stepik.android.presentation.course_reviews.CourseReviewsView
import javax.inject.Inject

class CourseReviewsStateMapper
@Inject
constructor() {

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
        val filteredReviews = reviews
            .dropWhile { courseReviewItem ->
                courseReviewItem is CourseReviewItem.ComposeBanner ||
                courseReviewItem is CourseReviewItem.Data && courseReviewItem.isCurrentUserReview
            }

        return PagedList(currentUserReview + filteredReviews, page = reviews.page, hasPrev = reviews.hasPrev, hasNext = reviews.hasNext)
    }
}