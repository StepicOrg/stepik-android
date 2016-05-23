package org.stepic.droid.web

import com.google.gson.annotations.SerializedName
import org.stepic.droid.model.Meta
import org.stepic.droid.model.comments.DiscussionProxy

data class DiscussionProxyResponse(
        val meta: Meta?,
        @SerializedName("discussion-proxies")
        val discussionProxies: List<DiscussionProxy>
)