package org.stepik.android.model.adaptive

import org.stepik.android.model.adaptive.Reaction


class RecommendationReaction(val lesson: Long, reaction: Reaction, var user: Long = 0) {
    private val reaction = reaction.value
}