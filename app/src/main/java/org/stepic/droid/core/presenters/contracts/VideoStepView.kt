package org.stepic.droid.core.presenters.contracts

import org.stepic.droid.model.Video

interface VideoStepView {

    fun onNeedOpenVideo(videoId: Long, cachedVideo: Video?, externalVideo: Video?)

    fun onVideoLoaded(thumbnailPath: String?, cachedVideo: Video?, externalVideo: Video?)

    fun onInternetProblem()
}
