package org.stepik.android.remote.discussion_thread.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.Meta
import org.stepik.android.model.comments.DiscussionThread
import org.stepik.android.remote.base.model.MetaResponse

class DiscussionThreadResponse(
    @SerializedName("meta")
    override val meta: Meta,
    @SerializedName("discussion-threads")
    val discussionThreads: List<DiscussionThread>
) : MetaResponse