package org.stepik.android.view.learning_actions.model

import org.stepik.android.presentation.user_reviews.UserReviewsFeature
import org.stepik.android.presentation.wishlist.WishlistFeature
import ru.nobird.app.core.model.Identifiable

sealed class LearningActionsItem {
    data class Wishlist(val state: WishlistFeature.State) : LearningActionsItem(), Identifiable<String> {
        override val id: String = "wishlist"
    }
    data class UserReviews(val state: UserReviewsFeature.State) : LearningActionsItem(), Identifiable<String> {
        override val id: String = "user_reviews"
    }
}