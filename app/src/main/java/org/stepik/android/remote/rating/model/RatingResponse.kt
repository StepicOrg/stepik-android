package org.stepik.android.remote.rating.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.adaptive.RatingItem

class RatingResponse(
    @SerializedName("count")
    val count: Long,
    @SerializedName("users")
    val users: List<RatingItem>
)