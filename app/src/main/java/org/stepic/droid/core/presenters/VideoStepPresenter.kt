package org.stepic.droid.core.presenters

import android.support.annotation.MainThread
import android.support.annotation.WorkerThread
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.core.presenters.contracts.VideoStepView
import org.stepic.droid.di.step.StepScope
import org.stepic.droid.model.Step
import org.stepic.droid.model.Video
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.util.resolvers.VideoResolver
import org.stepic.droid.web.Api
import java.util.concurrent.Semaphore
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@StepScope
class VideoStepPresenter
@Inject constructor(
        private val threadPoolExecutor: ThreadPoolExecutor,
        private val mainHandler: MainHandler,
        private val api: Api,
        private val databaseFacade: DatabaseFacade,
        private val videoResolver: VideoResolver,
        private val analytic: Analytic) : PresenterBase<VideoStepView>() {

    var video: Video? = null
    val videoInitiated = AtomicBoolean(false)

    @MainThread
    fun initVideo(step: Step) {
        val localVideo = step.block?.video

        if (localVideo != null) {
            video = localVideo
            view?.onVideoLoaded(localVideo.thumbnail, localVideo)
            return
        }

        if (videoInitiated.compareAndSet(false, true)) {

            threadPoolExecutor.execute {
                try {
                    val videoFromInternet: Video? = getVideoFromWeb(step.id)
                    //if null do not show warning, while user do not click
                    if (videoFromInternet != null) {
                        mainHandler.post {
                            video = videoFromInternet
                            view?.onVideoLoaded(videoFromInternet.thumbnail, videoFromInternet)
                        }
                    }
                } finally {
                    videoInitiated.set(false)
                }
            }
        }
    }

    val isVideoOpening = Semaphore(1)

    @MainThread
    fun playVideo(step: Step) {
        val localVideo = video
        threadPoolExecutor.execute {

            if (isVideoOpening.tryAcquire()) {
                try {
                    val videoForPlaying = localVideo ?: getVideoFromWeb(step.id)
                    if (videoForPlaying == null) {
                        mainHandler.post {
                            view?.onInternetProblem()
                        }
                    } else {
                        playVideo(videoForPlaying, step)
                    }
                } finally {
                    isVideoOpening.release()
                }
            }
        }
    }


    @WorkerThread
    private fun playVideo(video: Video, step: Step) {
        val url = videoResolver.resolveVideoUrl(video, step)
        if (url != null) {
            mainHandler.post { view?.onNeedOpenVideo(url, video.id) }
        } else {
            analytic.reportEventWithName(Analytic.Error.VIDEO_PATH_WAS_NULL_WITH_INTERNET, step.id.toString())
            mainHandler.post { view?.onInternetProblem() }
        }
    }


    @WorkerThread
    private fun getVideoFromWeb(stepId: Long): Video? {
        try {
            return api.getSteps(longArrayOf(stepId)).execute().body()?.steps?.firstOrNull()?.block?.video
        } catch (e: Exception) {
            return null // Internet is not available or video is not uploaded in web
        }
    }
}
