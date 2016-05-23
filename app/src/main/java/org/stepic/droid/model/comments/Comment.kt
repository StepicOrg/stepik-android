package org.stepic.droid.model.comments

import org.stepic.droid.model.Actions


data class Comment(
        val id: Long?,
        val parent: Long?,
        val user: Int?,
        val user_role: String?,
        val time: String?,
        val text: String,
        val reply_count: Int,
        var is_deleted: Boolean?,
        val deleted_by: String?,
        val deleted_at: String?,
        val can_moderate: Boolean?,
        val can_delete: Boolean?,
        val actions: Actions,
        val target: Long?, //for example, id of Step.
        val replies: List<Long>?,//list of all replies, but in query only 20.
        val tonality_auto: Int?,
        val tonality_manual: Int?,
        val is_pinned: Boolean?,
        val is_staff_replied: Boolean?,
        val is_reported: Boolean?,
        val epic_count: Int?,
        val abuse_count: Int?,
        val vote: String?
)