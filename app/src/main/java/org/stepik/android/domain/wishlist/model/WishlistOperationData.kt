package org.stepik.android.domain.wishlist.model

import org.stepik.android.presentation.wishlist.model.WishlistAction

data class WishlistOperationData(
    val courseId: Long,
    val wishlistAction: WishlistAction
)