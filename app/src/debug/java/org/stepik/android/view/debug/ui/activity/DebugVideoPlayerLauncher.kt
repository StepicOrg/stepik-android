package org.stepik.android.view.debug.ui.activity

import android.content.Context
import android.content.Intent
import org.stepik.android.model.Video
import org.stepik.android.model.VideoUrl
import org.stepik.android.view.video_player.model.VideoPlayerMediaData
import org.stepik.android.view.video_player.ui.activity.VideoPlayerActivity

object DebugVideoPlayerLauncher {
    private const val SAMPLE_VIDEO_URL =
        "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4"

    fun createIntent(context: Context): Intent =
        VideoPlayerActivity.createIntent(
            context,
            VideoPlayerMediaData(
                title = "Debug video",
                externalVideo = Video(
                    id = -1,
                    urls = listOf(VideoUrl(SAMPLE_VIDEO_URL, "720"))
                )
            )
        )
}
