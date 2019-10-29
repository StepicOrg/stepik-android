package org.stepik.android.remote.vote.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.Meta
import org.stepik.android.model.comments.Vote
import org.stepik.android.remote.base.model.MetaResponse

class VoteResponse(
    @SerializedName("meta")
    override val meta: Meta,
    @SerializedName("votes")
    val votes: List<Vote>
) : MetaResponse