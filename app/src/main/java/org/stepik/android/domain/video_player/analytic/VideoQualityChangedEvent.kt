package org.stepik.android.domain.video_player.analytic

import org.stepik.android.domain.base.analytic.AnalyticEvent

class VideoQualityChangedEvent(
    source: String,
    target: String
) : AnalyticEvent {
    companion object {
        private const val PARAM_SOURCE = "source"
        private const val PARAM_TARGET = "target"
    }

    override val name: String =
        "Video quality changed"

    override val params: Map<String, Any> =
        mapOf(
            PARAM_SOURCE to source,
            PARAM_TARGET to target
        )
}