package org.stepik.android.presentation.wishlist.reducer

import org.stepik.android.presentation.wishlist.WishlistFeature.State
import org.stepik.android.presentation.wishlist.WishlistFeature.Message
import org.stepik.android.presentation.wishlist.WishlistFeature.Action
import ru.nobird.android.presentation.redux.reducer.StateReducer
import javax.inject.Inject

class WishlistReducer
@Inject
constructor() : StateReducer<State, Message, Action> {
    override fun reduce(state: State, message: Message): Pair<State, Set<Action>> =
        when (message) {
            is Message.InitMessage -> {
                if (state is State.Idle) {
                    State.Loading to setOf(Action.FetchWishList)
                } else {
                    null
                }
            }

            is Message.FetchWishlistSuccess -> {
                if (state is State.Loading) {
                    if (message.wishListRecord.data.courses.isNullOrEmpty()) {
                        State.Empty to emptySet()
                    } else {
                        State.Content(message.wishListRecord.data.courses) to emptySet()
                    }
                } else {
                    null
                }
            }

            is Message.FetchWishListError -> {
                if (state is State.Loading) {
                    State.Error to emptySet()
                } else {
                    null
                }
            }
        } ?: state to emptySet()
}