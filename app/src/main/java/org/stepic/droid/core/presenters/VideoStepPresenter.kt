package org.stepic.droid.core.presenters

import android.support.annotation.MainThread
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.core.presenters.contracts.VideoStepView
import org.stepic.droid.persistence.model.StepPersistentWrapper
import timber.log.Timber
import javax.inject.Inject

class VideoStepPresenter
@Inject constructor(
        private val analytic: Analytic
) : PresenterBase<VideoStepView>() {
    @MainThread
    fun initVideo(stepWrapper: StepPersistentWrapper) {
        stepWrapper.step.block?.let {
            val thumbnail = stepWrapper.cachedVideo?.thumbnail ?: it.video?.thumbnail
            if (thumbnail == null) {

                //it should not be happened, it is here for fallback
                analytic.reportEventWithName(Analytic.Error.ILLEGAL_STATE_VIDEO_STEP, stepWrapper.step.id.toString())
                Timber.e(Analytic.Error.ILLEGAL_STATE_VIDEO_STEP)
                //external video can be null if user was used previous version of the app and enter to step without internet
                view?.onInternetProblem()
            } else {
                view?.onVideoLoaded(thumbnail, stepWrapper.cachedVideo, it.video)
            }
        }
    }

    @MainThread
    fun playVideo(stepWrapper: StepPersistentWrapper) {
        stepWrapper.step.block?.let {
            if (stepWrapper.cachedVideo == null && it.video == null) {
                analytic.reportEvent(Analytic.Error.ILLEGAL_STATE_VIDEO_STEP_PLAY)
                view?.onInternetProblem()
            } else {
                val videoId = stepWrapper.cachedVideo?.id ?: it.video?.id ?: 0L
                view?.onNeedOpenVideo(videoId, stepWrapper.cachedVideo, it.video)
            }
        }
    }
}
