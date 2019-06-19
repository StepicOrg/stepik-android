package org.stepik.android.presentation.video_player

import android.os.Bundle
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.preferences.VideoPlaybackRate
import org.stepic.droid.util.emptyOnErrorStub
import org.stepik.android.domain.video_player.interactor.VideoPlayerSettingsInteractor
import org.stepik.android.model.VideoUrl
import org.stepik.android.presentation.base.PresenterBase
import org.stepik.android.view.video_player.model.VideoPlayerData
import org.stepik.android.view.video_player.model.VideoPlayerMediaData
import javax.inject.Inject

class VideoPlayerPresenter
@Inject
constructor(
    private val analytic: Analytic,
    private val videoPlayerSettingsInteractor: VideoPlayerSettingsInteractor,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<VideoPlayerView>() {
    companion object {
        private const val VIDEO_PLAYER_DATA = "video_player_data"

        private const val TIMESTAMP_EPS = 1000L
    }

    private var videoPlayerData: VideoPlayerData? = null
        set(value) {
            field = value
            if (value != null) {
                view?.setVideoPlayerData(value)
            }
        }

    private var isRotateVideo: Boolean? = null
        set(value) {
            field = value
            view?.setIsRotateVideo(value ?: false)
        }

    private var isLoading = false

    init {
        compositeDisposable += videoPlayerSettingsInteractor
            .isRotateVideo()
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribe { isRotate ->
                isRotateVideo = isRotate
            }
    }

    override fun attachView(view: VideoPlayerView) {
        super.attachView(view)
        videoPlayerData?.let(view::setVideoPlayerData)
        view.setIsRotateVideo(isRotateVideo ?: false)
    }

    /**
     * Data initialization variants
     */
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        if (videoPlayerData == null) {
            videoPlayerData = savedInstanceState.getParcelable(VIDEO_PLAYER_DATA)
        }
    }

    fun onMediaData(mediaData: VideoPlayerMediaData) {
        if (videoPlayerData != null || isLoading) return

        compositeDisposable += videoPlayerSettingsInteractor
            .getVideoPlayerData(mediaData)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .doOnSubscribe {
                isLoading = true
            }
            .doFinally {
                isLoading = false
            }
            .subscribeBy(
                onSuccess = { videoPlayerData = it; resolveVideoInBackgroundPopup() },
                onError = emptyOnErrorStub
            )
    }

    private fun resolveVideoInBackgroundPopup() {
        compositeDisposable += videoPlayerSettingsInteractor
            .isFirstVideoPlayback()
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = { isFirstTime ->
                    if (isFirstTime) {
                        view?.showPlayInBackgroundPopup()
                    }
                },
                onError = emptyOnErrorStub
            )
    }

    /**
     * Video properties
     */
    fun changeQuality(videoUrl: VideoUrl?) {
        val playerData = videoPlayerData
            ?: return

        val url = videoUrl
            ?.url
            ?: return

        videoPlayerData = playerData.copy(videoUrl = url)
    }

    fun changePlaybackRate(videoPlaybackRate: VideoPlaybackRate) {
        val playerData = this.videoPlayerData
            ?: return

        analytic.reportAmplitudeEvent(
            AmplitudeAnalytic.Video.PLAYBACK_SPEED_CHANGED,
            mapOf(
                AmplitudeAnalytic.Video.Params.SOURCE to playerData.videoPlaybackRate.rateFloat,
                AmplitudeAnalytic.Video.Params.TARGET to videoPlaybackRate.rateFloat
            )
        )

        videoPlayerData = playerData.copy(videoPlaybackRate = videoPlaybackRate)

        compositeDisposable += videoPlayerSettingsInteractor
            .setPlaybackRate(videoPlaybackRate)
            .subscribeOn(backgroundScheduler)
            .subscribe()
    }

    fun changeVideoRotation(isRotateVideo: Boolean) {
        this.isRotateVideo = isRotateVideo

        compositeDisposable += videoPlayerSettingsInteractor
            .setRotateVideo(isRotateVideo)
            .subscribeOn(backgroundScheduler)
            .subscribe()
    }

    fun syncVideoTimestamp(currentPosition: Long, duration: Long) {
        val playerData = videoPlayerData
            ?: return

        val timestamp =
            if (duration > 0 && currentPosition + TIMESTAMP_EPS >= duration) {
                0L
            } else {
                currentPosition
            }

        compositeDisposable += videoPlayerSettingsInteractor
            .saveVideoTimestamp(playerData.videoId, timestamp)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(onError = emptyOnErrorStub)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(VIDEO_PLAYER_DATA, videoPlayerData)
    }
}