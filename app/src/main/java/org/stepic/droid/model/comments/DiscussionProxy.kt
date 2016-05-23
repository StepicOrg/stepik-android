package org.stepic.droid.model.comments

data class DiscussionProxy(
        val id: String,
        val discussions: List<Long>,
        val discussions_most_liked: List<Long>,
        val discussions_most_active: List<Long>,
        val discussions_recent_activity: List<Long>
)
