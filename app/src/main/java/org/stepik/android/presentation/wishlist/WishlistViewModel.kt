package org.stepik.android.presentation.wishlist

import ru.nobird.android.presentation.redux.container.ReduxViewContainer
import ru.nobird.android.view.redux.viewmodel.ReduxViewModel

class WishlistViewModel(
    reduxViewContainer: ReduxViewContainer<WishlistFeature.State, WishlistFeature.Message, WishlistFeature.Action.ViewAction>
) : ReduxViewModel<WishlistFeature.State, WishlistFeature.Message, WishlistFeature.Action.ViewAction>(reduxViewContainer)