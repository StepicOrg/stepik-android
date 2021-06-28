package org.stepik.android.domain.wishlist.analytic

import org.stepik.android.domain.base.analytic.AnalyticEvent

object WishlistOpenedEvent : AnalyticEvent {
    override val name: String =
        "Wishlist screen opened"
}