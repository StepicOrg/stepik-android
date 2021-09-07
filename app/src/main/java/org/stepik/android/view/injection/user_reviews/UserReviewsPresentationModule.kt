package org.stepik.android.view.injection.user_reviews

import androidx.lifecycle.ViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.user_reviews.UserReviewsFeature
import org.stepik.android.presentation.user_reviews.UserReviewsViewModel
import org.stepik.android.presentation.user_reviews.dispatcher.UserReviewsActionDispatcher
import org.stepik.android.presentation.user_reviews.reducer.UserReviewsReducer
import org.stepik.android.presentation.wishlist.WishlistFeature
import org.stepik.android.presentation.wishlist.WishlistViewModel
import org.stepik.android.presentation.wishlist.dispatcher.WishlistActionDispatcher
import org.stepik.android.presentation.wishlist.reducer.WishlistReducer
import ru.nobird.android.presentation.redux.container.wrapWithViewContainer
import ru.nobird.android.presentation.redux.dispatcher.wrapWithActionDispatcher
import ru.nobird.android.presentation.redux.feature.Feature
import ru.nobird.android.presentation.redux.feature.ReduxFeature

@Module
object UserReviewsPresentationModule {
    /**
     * Presentation
     */
    @Provides
    @IntoMap
    @ViewModelKey(UserReviewsViewModel::class)
    internal fun provideUserReviewsPresenter(
        userReviewsFeature: Feature<UserReviewsFeature.State, UserReviewsFeature.Message, UserReviewsFeature.Action>
    ): ViewModel =
        UserReviewsViewModel(userReviewsFeature.wrapWithViewContainer())

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

    @Provides
    @LearningActionsScope
    internal fun provideUserReviewsFeature(
        userReviewsReducer: UserReviewsReducer,
        userReviewsActionDispatcher: UserReviewsActionDispatcher
    ): Feature<UserReviewsFeature.State, UserReviewsFeature.Message, UserReviewsFeature.Action> =
        ReduxFeature(UserReviewsFeature.State.Idle, userReviewsReducer)
            .wrapWithActionDispatcher(userReviewsActionDispatcher)
}