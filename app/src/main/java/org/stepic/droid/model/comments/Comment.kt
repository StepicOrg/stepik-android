package org.stepic.droid.model.comments

import com.google.gson.annotations.SerializedName
import org.stepic.droid.model.Actions
import org.stepic.droid.model.UserRole


data class Comment(
        val id: Long? = null,
        var parent: Long? = null,
        val user: Int? = null,
        @SerializedName("user_role")
        val userRole: UserRole? = null,
        val time: String? = null,
        var text: String = "",
        @SerializedName("reply_count")
        val replyCount: Int? = null,
        var is_deleted: Boolean? = null,
        val deleted_by: String? = null,
        val deleted_at: String? = null,
        val can_moderate: Boolean? = null,
        val can_delete: Boolean? = null,
        val actions: Actions? = null,
        var target: Long? = null, //for example, id of Step.
        val replies: List<Long>? = null, //oldList of all replies, but in query only 20.
        val tonality_auto: Int? = null,
        val tonality_manual: Int? = null,
        @SerializedName("is_pinned")
        val isPinned: Boolean = false,
        val is_staff_replied: Boolean? = null,
        val is_reported: Boolean? = null,
        val epic_count: Int? = null,
        val abuse_count: Int? = null,
        val vote: String? = null
) {
    constructor(target: Long?, text: String, parent: Long?) : this() {
        this.target = target
        this.text = text
        this.parent = parent
    }
}