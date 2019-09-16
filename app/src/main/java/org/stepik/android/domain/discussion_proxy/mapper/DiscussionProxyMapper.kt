package org.stepik.android.domain.discussion_proxy.mapper

import org.stepik.android.domain.discussion_proxy.model.DiscussionOrder
import org.stepik.android.model.comments.DiscussionProxy

fun DiscussionProxy.getOrdering(order: DiscussionOrder): List<Long> =
    when (order) {
        DiscussionOrder.LAST_DISCUSSION ->
            discussions

        DiscussionOrder.MOST_LIKED ->
            discussionsMostLiked

        DiscussionOrder.MOST_ACTIVE ->
            discussionsMostActive

        DiscussionOrder.RECENT_ACTIVITY ->
            discussionsRecentActivity
    }
