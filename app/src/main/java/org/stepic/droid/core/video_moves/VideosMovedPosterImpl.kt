package org.stepic.droid.core.video_moves

import org.stepic.droid.base.ListenerContainer
import org.stepic.droid.core.video_moves.contract.VideosMovedListener
import org.stepic.droid.core.video_moves.contract.VideosMovedPoster
import javax.inject.Inject

class VideosMovedPosterImpl
@Inject constructor(
        private val listenerContainer: ListenerContainer<VideosMovedListener>)
    : VideosMovedPoster {
    override fun videosMoved() {
        listenerContainer.asIterable().forEach(VideosMovedListener::onVideosMoved)
    }
}
