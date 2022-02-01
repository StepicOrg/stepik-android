package org.stepik.android.view.injection.wishlist

import androidx.lifecycle.ViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.wishlist.WishlistFeature
import org.stepik.android.presentation.wishlist.WishlistViewModel
import org.stepik.android.presentation.wishlist.dispatcher.WishlistActionDispatcher
import org.stepik.android.presentation.wishlist.reducer.WishlistReducer
import ru.nobird.app.presentation.redux.container.wrapWithViewContainer
import ru.nobird.app.presentation.redux.dispatcher.wrapWithActionDispatcher
import ru.nobird.app.presentation.redux.feature.ReduxFeature

@Module
object WishlistPresentationModule {
    /**
     * Presentation
     */
    @Provides
    @IntoMap
    @ViewModelKey(WishlistViewModel::class)
    internal fun provideWishlistPresenter(
        wishlistReducer: WishlistReducer,
        wishlistActionDispatcher: WishlistActionDispatcher
    ): ViewModel =
        WishlistViewModel(
            ReduxFeature(WishlistFeature.State.Idle, wishlistReducer)
                .wrapWithActionDispatcher(wishlistActionDispatcher)
                .wrapWithViewContainer()
        )
}