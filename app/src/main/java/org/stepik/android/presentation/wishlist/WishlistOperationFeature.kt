package org.stepik.android.presentation.wishlist

import org.stepik.android.domain.base.analytic.AnalyticEvent
import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.domain.wishlist.model.WishlistOperationData
import org.stepik.android.model.Course

interface WishlistOperationFeature {
    sealed class State {
        object Idle : State()
        object Adding : State()
        object Wishlisted : State()
    }

    sealed class Message {
        data class WishlistAddMessage(val course: Course, val courseViewSource: CourseViewSource) : Message()
        data class WishlistAddSuccess(val course: Course, val courseViewSource: CourseViewSource) : Message()
        object WishlistAddFailure : Message()
    }

    sealed class Action {
        data class AddToWishlist(
            val course: Course,
            val courseViewSource: CourseViewSource,
            val wishlistOperationData: WishlistOperationData
        ) : Action()
        data class LogAnalyticEvent(val analyticEvent: AnalyticEvent) : Action()

        sealed class ViewAction : Action()
    }
}