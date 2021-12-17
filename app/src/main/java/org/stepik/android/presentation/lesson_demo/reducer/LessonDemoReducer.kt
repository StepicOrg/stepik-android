package org.stepik.android.presentation.lesson_demo.reducer

import org.stepik.android.presentation.course_purchase.model.CoursePurchaseDataResult
import org.stepik.android.presentation.lesson_demo.LessonDemoFeature
import org.stepik.android.presentation.lesson_demo.LessonDemoFeature.State
import org.stepik.android.presentation.lesson_demo.LessonDemoFeature.Message
import org.stepik.android.presentation.lesson_demo.LessonDemoFeature.Action
import org.stepik.android.presentation.wishlist.WishlistOperationFeature
import org.stepik.android.presentation.wishlist.reducer.WishlistOperationReducer
import ru.nobird.android.presentation.redux.reducer.StateReducer
import javax.inject.Inject

class LessonDemoReducer
@Inject
constructor(
    private val wishlistOperationReducer: WishlistOperationReducer
) : StateReducer<State, Message, Action> {
    override fun reduce(state: State, message: Message): Pair<State, Set<Action>> =
        when (message) {
            is Message.InitMessage -> {
                if (state is State.Idle || (state is State.Content && state.lessonDemoState is LessonDemoFeature.LessonDemoState.Error && message.forceUpdate)) {
                    val wishlistOperationState = if (message.course.isInWishlist) {
                        WishlistOperationFeature.State.Wishlisted
                    } else {
                        WishlistOperationFeature.State.Idle
                    }
                    State.Content(message.course, LessonDemoFeature.LessonDemoState.Loading, wishlistOperationState) to
                        setOf(Action.FetchLessonDemoData(message.course))
                } else {
                    null
                }
            }

            is Message.FetchLessonDemoDataSuccess -> {
                if (state is State.Content && state.lessonDemoState is LessonDemoFeature.LessonDemoState.Loading) {
                    val newLessonDemoState =
                        if (message.coursePurchaseDataResult is CoursePurchaseDataResult.NotAvailable) {
                            LessonDemoFeature.LessonDemoState.Unavailable
                        } else {
                            LessonDemoFeature.LessonDemoState.Content(message.deeplinkPromoCode, (message.coursePurchaseDataResult as? CoursePurchaseDataResult.Result)?.coursePurchaseData)
                        }
                    state.copy(lessonDemoState = newLessonDemoState) to emptySet()
                } else {
                    null
                }
            }
            is Message.FetchLessonDemoDataFailure -> {
                if (state is State.Content && state.lessonDemoState is LessonDemoFeature.LessonDemoState.Loading) {
                    state.copy(lessonDemoState = LessonDemoFeature.LessonDemoState.Error) to emptySet()
                } else {
                    null
                }
            }
            is Message.BuyActionMessage -> {
                if (state is State.Content && state.lessonDemoState is LessonDemoFeature.LessonDemoState.Content) {
                    state to setOf(Action.ViewAction.BuyAction(state.lessonDemoState.deeplinkPromoCode, state.lessonDemoState.coursePurchaseData))
                } else {
                    null
                }
            }
            is Message.WishlistMessage -> {
                if (state is State.Content &&
                    (state.lessonDemoState is LessonDemoFeature.LessonDemoState.Unavailable ||
                        state.lessonDemoState is LessonDemoFeature.LessonDemoState.Content)
                ) {
                    val (wishlistOperationState, wishlistOperationActions) = wishlistOperationReducer.reduce(state.wishlistOperationState, message.wishlistMessage)
                    val newState = if (message.wishlistMessage is WishlistOperationFeature.Message.WishlistAddSuccess) {
                        val updatedCourseModel = state.course.copy(isInWishlist = true)
                        if (state.lessonDemoState is LessonDemoFeature.LessonDemoState.Content) {
                            state.copy(
                                course = updatedCourseModel,
                                lessonDemoState = state.lessonDemoState.copy(
                                    coursePurchaseData = state.lessonDemoState.coursePurchaseData?.copy(isWishlisted = true)
                                ),
                                wishlistOperationState = wishlistOperationState
                            )
                        } else {
                            state.copy(course = updatedCourseModel, wishlistOperationState = wishlistOperationState)
                        }
                    } else {
                        state.copy(wishlistOperationState = wishlistOperationState)
                    }
                    newState to wishlistOperationActions.map(Action::WishlistAction).toSet()
                } else {
                    null
                }
            }
        } ?: state to emptySet()
}