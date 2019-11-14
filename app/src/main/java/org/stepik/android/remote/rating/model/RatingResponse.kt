package org.stepik.android.remote.rating.model

import org.stepik.android.model.adaptive.RatingItem

class RatingResponse(
    val count: Long,
    val users: List<RatingItem>
)