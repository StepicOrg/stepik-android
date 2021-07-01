package org.stepik.android.domain.video_player.analytic

import org.stepik.android.domain.base.analytic.AnalyticEvent

class VideoPlayerControlClickedEvent(
    action: String
) : AnalyticEvent {
    companion object {
        private const val PARAM_ACTION = "action"

        const val ACTION_PREVIOS = "previos"
        const val ACTION_REWIND = "rewind"
        const val ACTION_FORWARD = "forward"
        const val ACTION_NEXT = "next"
        const val ACTION_SEEK_BACK = "seek_back"
        const val ACTION_SEEK_FORWARD = "seek_forward"
        const val ACTION_DOUBLE_CLICK_LEFT = "double_click_left"
        const val ACTION_DOUBLE_CLICK_RIGHT = "double_click_right"
        const val ACTION_PLAY = "play"
        const val ACTION_PAUSE = "pause"
    }

    override val name: String =
        "Video player control clicked"

    override val params: Map<String, Any> =
        mapOf(PARAM_ACTION to action)
}