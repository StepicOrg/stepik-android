package org.stepik.android.view.injection.user_reviews

import androidx.lifecycle.ViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.redux.wrapWithRefCounter
import org.stepik.android.presentation.user_reviews.UserReviewsFeature
import org.stepik.android.presentation.user_reviews.UserReviewsViewModel
import org.stepik.android.presentation.user_reviews.dispatcher.UserReviewsActionDispatcher
import org.stepik.android.presentation.user_reviews.reducer.UserReviewsReducer
import ru.nobird.app.presentation.redux.container.wrapWithViewContainer
import ru.nobird.app.presentation.redux.dispatcher.wrapWithActionDispatcher
import ru.nobird.app.presentation.redux.feature.Feature
import ru.nobird.app.presentation.redux.feature.ReduxFeature

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
    @LearningActionsScope
    internal fun provideUserReviewsFeature(
        userReviewsReducer: UserReviewsReducer,
        userReviewsActionDispatcher: UserReviewsActionDispatcher
    ): Feature<UserReviewsFeature.State, UserReviewsFeature.Message, UserReviewsFeature.Action> =
        ReduxFeature(UserReviewsFeature.State.Idle, userReviewsReducer)
            .wrapWithActionDispatcher(userReviewsActionDispatcher)
            .wrapWithRefCounter()
}