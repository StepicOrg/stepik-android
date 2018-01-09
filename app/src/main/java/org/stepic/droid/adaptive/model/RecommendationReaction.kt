package org.stepic.droid.adaptive.model


class RecommendationReaction(val lessonId: Long, reaction: Reaction, var userId: Long = 0) {
    private val reaction = reaction.value
}