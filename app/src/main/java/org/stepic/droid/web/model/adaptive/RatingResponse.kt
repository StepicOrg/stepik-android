package org.stepic.droid.web.model.adaptive

import org.stepik.android.model.adaptive.RatingItem

class RatingResponse(
        val count: Long,
        val users: List<RatingItem>)