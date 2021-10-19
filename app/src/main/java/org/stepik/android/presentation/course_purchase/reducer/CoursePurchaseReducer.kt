package org.stepik.android.presentation.course_purchase.reducer

import org.stepik.android.domain.course_payments.model.DefaultPromoCode
import org.stepik.android.domain.wishlist.model.WishlistOperationData
import org.stepik.android.presentation.course_purchase.CoursePurchaseFeature.State
import org.stepik.android.presentation.course_purchase.CoursePurchaseFeature.Message
import org.stepik.android.presentation.course_purchase.CoursePurchaseFeature.Action
import org.stepik.android.presentation.course_purchase.CoursePurchaseFeature
import org.stepik.android.presentation.wishlist.model.WishlistAction
import org.stepik.android.view.course.resolver.CoursePromoCodeResolver
import ru.nobird.android.core.model.mutate
import ru.nobird.android.presentation.redux.reducer.StateReducer
import javax.inject.Inject

class CoursePurchaseReducer
@Inject
constructor(
    private val coursePromoCodeResolver: CoursePromoCodeResolver
) : StateReducer<State, Message, Action> {
    override fun reduce(state: State, message: Message): Pair<State, Set<Action>> =
        when (message) {
            is Message.InitMessage -> {
                if (state is State.Idle) {
                    val promoCodeState = if (message.initialCoursePromoCodeInfo.hasPromo) {
                        CoursePurchaseFeature.PromoCodeState.Valid(message.initialCoursePromoCodeInfo.name, message.initialCoursePromoCodeInfo)
                    } else {
                        CoursePurchaseFeature.PromoCodeState.Idle
                    }
                    val wishlistState = if (message.coursePurchaseData.isWishlisted) {
                        CoursePurchaseFeature.WishlistState.Wishlisted
                    } else {
                        CoursePurchaseFeature.WishlistState.Idle
                    }
                    State.Content(message.coursePurchaseData, promoCodeState, wishlistState) to emptySet()
                } else {
                    null
                }
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
            is Message.PromoCodeEditingMessage -> {
                if (state is State.Content) {
                    state.copy(promoCodeState = CoursePurchaseFeature.PromoCodeState.Editing) to emptySet()
                } else {
                    null
                }
            }
            is Message.PromoCodeCheckMessage -> {
                if (state is State.Content && state.promoCodeState is CoursePurchaseFeature.PromoCodeState.Editing) {
                    state.copy(promoCodeState = CoursePurchaseFeature.PromoCodeState.Checking(message.text)) to setOf(Action.CheckPromoCode(state.coursePurchaseData.course.id, message.text))
                } else {
                    null
                }
            }
            is Message.PromoCodeValidMessage -> {
                if (state is State.Content && state.promoCodeState is CoursePurchaseFeature.PromoCodeState.Checking) {
                    val coursePromoCodeInfo = coursePromoCodeResolver.resolvePromoCodeInfo(message.deeplinkPromoCode, DefaultPromoCode.EMPTY, state.coursePurchaseData.course)
                    state.copy(promoCodeState = CoursePurchaseFeature.PromoCodeState.Valid(state.promoCodeState.text, coursePromoCodeInfo)) to emptySet()
                } else {
                    null
                }
            }
            is Message.PromoCodeInvalidMessage -> {
                if (state is State.Content && state.promoCodeState is CoursePurchaseFeature.PromoCodeState.Checking) {
                    state.copy(promoCodeState = CoursePurchaseFeature.PromoCodeState.Invalid) to emptySet()
                } else {
                    null
                }
            }
        } ?: state to emptySet()
}