package org.stepik.android.presentation.video_player

import android.os.Bundle
import io.reactivex.Scheduler
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.model.VideoUrl
import org.stepik.android.presentation.base.PresenterBase
import org.stepik.android.view.video_player.model.VideoPlayerData
import org.stepik.android.view.video_player.model.VideoPlayerMediaData
import javax.inject.Inject

class VideoPlayerPresenter
@Inject
constructor(
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<VideoPlayerView>() {
    companion object {
        private const val VIDEO_PLAYER_DATA = "video_player_data"
    }

    private var videoPlayerData: VideoPlayerData? = null
        set(value) {
            field = value
            if (value != null) {
                view?.setVideoPlayerData(value)
            }
        }

    private var isLoading = false

    override fun attachView(view: VideoPlayerView) {
        super.attachView(view)
        videoPlayerData?.let(view::setVideoPlayerData)
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

        isLoading = true

    }

    fun changeQuality(videoUrl: VideoUrl?) {
        val playerData = this.videoPlayerData
            ?: return

        val url = videoUrl
            ?.url
            ?: return

        videoPlayerData = playerData.copy(videoUrl = url)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(VIDEO_PLAYER_DATA, videoPlayerData)
    }
}