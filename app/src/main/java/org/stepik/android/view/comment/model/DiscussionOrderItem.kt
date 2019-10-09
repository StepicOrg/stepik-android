package org.stepik.android.view.comment.model

import androidx.annotation.IdRes
import org.stepic.droid.R
import org.stepik.android.domain.discussion_proxy.model.DiscussionOrder

enum class DiscussionOrderItem(
    @IdRes
    val itemId: Int,
    val order: DiscussionOrder
) {
    LAST_DISCUSSION(R.id.menu_item_last_discussion, DiscussionOrder.LAST_DISCUSSION),
    MOST_LIKED(R.id.menu_item_most_liked, DiscussionOrder.MOST_LIKED),
    MOST_ACTIVE(R.id.menu_item_most_active, DiscussionOrder.MOST_ACTIVE),
    RECENT_ACTIVITY(R.id.menu_item_recent_activity, DiscussionOrder.RECENT_ACTIVITY);

    companion object {
        fun getBy(order: DiscussionOrder): DiscussionOrderItem =
            values().first { it.order == order }

        fun getById(@IdRes itemId: Int): DiscussionOrderItem? =
            values().find { it.itemId == itemId }
    }
}