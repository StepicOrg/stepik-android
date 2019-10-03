package org.stepik.android.presentation.video_player

import org.stepik.android.view.video_player.model.VideoPlayerData

interface VideoPlayerView {
    sealed class State {
        object Idle : State()
        object NextPending : State()
        object NextCancelled : State()
        object Next : State()
    }

    fun setState(state: State)

    fun setVideoPlayerData(videoPlayerData: VideoPlayerData)
    fun setIsLandscapeVideo(isLandScapeVideo: Boolean)
    fun showPlayInBackgroundPopup()
}