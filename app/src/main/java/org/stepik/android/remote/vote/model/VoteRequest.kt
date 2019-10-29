package org.stepik.android.remote.vote.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.comments.Vote

class VoteRequest(
    @SerializedName("vote")
    val vote: Vote
)