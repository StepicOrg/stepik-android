package org.stepic.droid.model.adaptive


class RecommendationReaction(val lessonId: Long, reaction: Reaction, var userId: Long = 0) {
    private val reaction = reaction.value
}