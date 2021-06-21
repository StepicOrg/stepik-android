package org.stepik.android.domain.course.mapper

import org.stepik.android.domain.course.model.EnrollmentState
import org.stepik.android.presentation.course.CourseView
import org.stepik.android.presentation.wishlist.model.WishlistAction
import ru.nobird.android.core.model.safeCast
import javax.inject.Inject

class CourseStateMapper
@Inject
constructor(
    val courseStatsMapper: CourseStatsMapper
) {
    inline fun mutateEnrolledState(state: CourseView.State, mutation: EnrollmentState.Enrolled.() -> EnrollmentState): CourseView.State {
        val oldState = state.safeCast<CourseView.State.CourseLoaded>()
            ?: return state

        return CourseView.State.CourseLoaded(oldState.courseHeaderData.copy(stats = courseStatsMapper.mutateEnrolledState(oldState.courseHeaderData.stats, mutation)))
    }

    fun mutateWishlistState(state: CourseView.State, wishlistAction: WishlistAction): CourseView.State {
        val oldState = state.safeCast<CourseView.State.CourseLoaded>()
            ?: return state

        val isWishlisted = wishlistAction == WishlistAction.ADD

        return CourseView.State.CourseLoaded(oldState.courseHeaderData.copy(stats = oldState.courseHeaderData.stats.copy(isWishlisted = isWishlisted)))
    }
}