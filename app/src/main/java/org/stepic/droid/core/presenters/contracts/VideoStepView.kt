package org.stepic.droid.core.presenters.contracts

import org.stepic.droid.model.Video

interface VideoStepView {

    fun onNeedOpenVideo(pathToVideo: String, videoId: Long)

    fun onVideoLoaded (thumbnailPath : String?, video : Video)

    fun onInternetProblem()
}
