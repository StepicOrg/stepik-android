package org.stepik.android.presentation.course_purchase.reducer

import org.stepik.android.domain.wishlist.model.WishlistOperationData
import org.stepik.android.presentation.course_purchase.CoursePurchaseFeature.State
import org.stepik.android.presentation.course_purchase.CoursePurchaseFeature.Message
import org.stepik.android.presentation.course_purchase.CoursePurchaseFeature.Action
import org.stepik.android.presentation.course_purchase.CoursePurchaseFeature
import org.stepik.android.presentation.wishlist.model.WishlistAction
import ru.nobird.android.core.model.mutate
import ru.nobird.android.presentation.redux.reducer.StateReducer
import javax.inject.Inject

class CoursePurchaseReducer
@Inject
constructor() : StateReducer<State, Message, Action> {
    override fun reduce(state: State, message: Message): Pair<State, Set<Action>> =
        when (message) {
            is Message.InitMessage -> {
                val promoCodeState = CoursePurchaseFeature.PromoCodeState.Idle
                val wishlistState = if (message.coursePurchaseData.isWishlisted) {
                    CoursePurchaseFeature.WishlistState.Wishlisted
                } else {
                    CoursePurchaseFeature.WishlistState.Idle
                }
                State.Content(message.coursePurchaseData, promoCodeState, wishlistState) to emptySet()
            }
            is Message.WishlistAddMessage -> {
                if (state is State.Content) {
                    val wishlistEntity = state.coursePurchaseData.wishlistEntity.copy(courses = state.coursePurchaseData.wishlistEntity.courses.mutate { add(0, state.coursePurchaseData.course.id) })
                    val wishlistOperationData = WishlistOperationData(state.coursePurchaseData.course.id, WishlistAction.ADD)
                    state.copy(wishlistState = CoursePurchaseFeature.WishlistState.Adding)to setOf(Action.AddToWishlist(state.coursePurchaseData.course, wishlistEntity, wishlistOperationData))
                } else {
                    null
                }
            }
            is Message.WishlistAddSuccess -> {
                if (state is State.Content) {
                    val updatedCoursePurchaseData = state.coursePurchaseData.copy(wishlistEntity = message.wishlistEntity, isWishlisted = true)
                    state.copy(coursePurchaseData = updatedCoursePurchaseData, wishlistState = CoursePurchaseFeature.WishlistState.Wishlisted) to emptySet()
                } else {
                    null
                }
            }
            is Message.WishlistAddFailure -> {
                if (state is State.Content) {
                    state.copy(wishlistState = CoursePurchaseFeature.WishlistState.Idle) to emptySet()
                } else {
                    null
                }
            }
        } ?: state to emptySet()
}