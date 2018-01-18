package org.stepic.droid.adaptive.model


class RecommendationReaction(val lesson: Long, reaction: Reaction, var user: Long = 0) {
    private val reaction = reaction.value
}