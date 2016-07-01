package org.stepic.droid.model.comments

import android.support.annotation.StringRes
import org.stepic.droid.R

enum class DiscussionOrder private constructor(val id: Int) {
    lastDiscussion(0),
    mostLiked(1),
    mostActive(2),
    recentActive(3);

    fun getOrder(dp: DiscussionProxy) =
            when (id) {
                0 -> dp.discussions
                1 -> dp.discussions_most_liked
                2 -> dp.discussions_most_active
                3 -> dp.discussions_recent_activity
                else -> dp.discussions
            }

    @StringRes
    fun getStringId() = when (id) {
        0 -> R.string.last_discussion
        1 -> R.string.most_liked_discussion
        2 -> R.string.most_active_discussion
        3 -> R.string.recent_activity_discussion
        else -> R.string.last_discussion
    }

}