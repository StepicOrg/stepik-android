package org.stepik.android.presentation.wishlist.reducer

import org.stepik.android.domain.wishlist.analytic.CourseWishlistAddedEvent
import org.stepik.android.domain.wishlist.model.WishlistOperationData
import org.stepik.android.presentation.wishlist.WishlistOperationFeature.State
import org.stepik.android.presentation.wishlist.WishlistOperationFeature.Message
import org.stepik.android.presentation.wishlist.WishlistOperationFeature.Action
import org.stepik.android.presentation.wishlist.model.WishlistAction
import ru.nobird.app.presentation.redux.reducer.StateReducer
import javax.inject.Inject

class WishlistOperationReducer
@Inject
constructor() : StateReducer<State, Message, Action> {
    override fun reduce(state: State, message: Message): Pair<State, Set<Action>> =
        when (message) {
            is Message.WishlistAddMessage -> {
                if (state is State.Idle) {
                    val wishlistOperationData = WishlistOperationData(message.course.id, WishlistAction.ADD)
                    State.Adding to setOf(Action.AddToWishlist(message.course, message.courseViewSource, wishlistOperationData))
                } else {
                    null
                }
            }
            is Message.WishlistAddSuccess -> {
                if (state is State.Adding) {
                    State.Wishlisted to
                        setOf(
                            Action.LogAnalyticEvent(
                                CourseWishlistAddedEvent(
                                    message.course,
                                    message.courseViewSource
                                )
                            )
                        )
                } else {
                    null
                }
            }
            is Message.WishlistAddFailure -> {
                if (state is State.Adding) {
                    State.Idle to emptySet()
                } else {
                    null
                }
            }
        } ?: state to emptySet()
}