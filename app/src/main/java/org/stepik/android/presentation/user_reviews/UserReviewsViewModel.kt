package org.stepik.android.presentation.user_reviews

import ru.nobird.app.presentation.redux.container.ReduxViewContainer
import ru.nobird.android.view.redux.viewmodel.ReduxViewModel

class UserReviewsViewModel(
    reduxViewContainer: ReduxViewContainer<UserReviewsFeature.State, UserReviewsFeature.Message, UserReviewsFeature.Action.ViewAction>
) : ReduxViewModel<UserReviewsFeature.State, UserReviewsFeature.Message, UserReviewsFeature.Action.ViewAction>(reduxViewContainer)