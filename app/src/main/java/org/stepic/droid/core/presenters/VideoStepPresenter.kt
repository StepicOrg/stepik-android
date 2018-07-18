package org.stepic.droid.core.presenters

import android.support.annotation.MainThread
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.core.presenters.contracts.VideoStepView
import org.stepik.android.model.structure.Step
import org.stepic.droid.util.VideoFileResolver
import timber.log.Timber
import javax.inject.Inject

class VideoStepPresenter
@Inject constructor(
        private val analytic: Analytic,
        private val videoFileResolver: VideoFileResolver
) : PresenterBase<VideoStepView>() {


    @MainThread
    fun initVideo(step: Step) {
        step.block?.let {
            val thumbnail = it.cachedLocalVideo?.thumbnail ?: it.video?.thumbnail
            if (thumbnail == null) {

                //it should not be happened, it is here for fallback
                analytic.reportEventWithName(Analytic.Error.ILLEGAL_STATE_VIDEO_STEP, step.id.toString())
                Timber.e(Analytic.Error.ILLEGAL_STATE_VIDEO_STEP)
                //external video can be null if user was used previous version of the app and enter to step without internet
                view?.onInternetProblem()
            } else {
                view?.onVideoLoaded(thumbnail, it.cachedLocalVideo, it.video)
            }
        }
    }

    @MainThread
    fun playVideo(step: Step) {
        step.block?.let {
            if (it.cachedLocalVideo == null && it.video == null) {
                analytic.reportEvent(Analytic.Error.ILLEGAL_STATE_VIDEO_STEP_PLAY)
                view?.onInternetProblem()
            } else {
                val videoId = it.cachedLocalVideo?.id ?: it.video?.id ?: 0L
                view?.onNeedOpenVideo(videoId, videoFileResolver.resolveVideoFile(it.cachedLocalVideo, step.id), it.video)
            }
        }
    }
}
