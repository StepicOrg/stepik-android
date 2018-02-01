package org.stepic.droid.web.model.adaptive

import org.stepic.droid.adaptive.model.RatingItem

class RatingResponse(
        val count: Long,
        val users: List<RatingItem>)