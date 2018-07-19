package org.stepic.droid.web

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.comments.DiscussionProxy
import org.stepik.android.model.Meta

data class DiscussionProxyResponse(
        val meta: Meta?,
        @SerializedName("discussion-proxies")
        val discussionProxies: List<DiscussionProxy>
)