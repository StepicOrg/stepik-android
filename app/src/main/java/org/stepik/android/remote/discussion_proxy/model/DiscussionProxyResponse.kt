package org.stepik.android.remote.discussion_proxy.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.Meta
import org.stepik.android.model.comments.DiscussionProxy
import org.stepik.android.remote.base.model.MetaResponse

class DiscussionProxyResponse(
    @SerializedName("meta")
    override val meta: Meta,
    @SerializedName("discussion-proxies")
    val discussionProxies: List<DiscussionProxy>
) : MetaResponse