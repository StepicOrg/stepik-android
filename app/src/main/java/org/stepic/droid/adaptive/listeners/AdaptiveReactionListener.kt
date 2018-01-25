package org.stepic.droid.adaptive.listeners

import org.stepic.droid.adaptive.model.Reaction

interface AdaptiveReactionListener {
    fun createReaction(lessonId: Long, reaction: Reaction)
}
