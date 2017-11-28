package org.stepic.droid.core.videomoves

import org.stepic.droid.base.ListenerContainer
import org.stepic.droid.core.videomoves.contract.VideosMovedListener
import org.stepic.droid.core.videomoves.contract.VideosMovedPoster
import javax.inject.Inject

class VideosMovedPosterImpl
@Inject constructor(
        private val listenerContainer: ListenerContainer<VideosMovedListener>)
    : VideosMovedPoster {
    override fun videosMoved() {
        listenerContainer.asIterable().forEach(VideosMovedListener::onVideosMoved)
    }
}
