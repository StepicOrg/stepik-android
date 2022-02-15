package org.stepik.android.presentation.banner.reducer

import org.stepik.android.presentation.banner.BannerFeature.State
import org.stepik.android.presentation.banner.BannerFeature.Message
import org.stepik.android.presentation.banner.BannerFeature.Action
import ru.nobird.app.presentation.redux.reducer.StateReducer
import javax.inject.Inject

class BannerReducer
@Inject
constructor() : StateReducer<State, Message, Action> {
    override fun reduce(state: State, message: Message): Pair<State, Set<Action>> =
        when (message) {
            is Message.InitMessage -> {
                if (state is State.Idle || message.forceUpdate) {
                    State.Loading to setOf(Action.LoadBanners(message.language, message.screen))
                } else {
                    null
                }
            }
            is Message.BannersError -> {
                if (state is State.Loading) {
                    State.Empty to emptySet()
                } else {
                    null
                }
            }
            is Message.BannersResult -> {
                if (state is State.Loading) {
                    val newState =
                        if (message.banners.isEmpty()) {
                            State.Empty
                        } else {
                            State.Content(message.banners)
                        }
                    newState to emptySet()
                } else {
                    null
                }
            }
        } ?: state to emptySet()
}