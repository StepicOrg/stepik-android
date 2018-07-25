package org.stepic.droid.core.presenters.contracts

import org.stepik.android.model.Video

interface VideoStepView {

    fun onNeedOpenVideo(videoId: Long, cachedVideo: Video?, externalVideo: Video?)

    fun onVideoLoaded(thumbnailPath: String?, cachedVideo: Video?, externalVideo: Video?)

    fun onInternetProblem()
}
