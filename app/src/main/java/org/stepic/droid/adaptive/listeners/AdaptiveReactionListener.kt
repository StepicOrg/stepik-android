package org.stepic.droid.adaptive.listeners

import org.stepik.android.model.adaptive.Reaction

interface AdaptiveReactionListener {
    fun createReaction(lessonId: Long, reaction: Reaction)
}
