package org.stepik.android.domain.video_player.analytic

import org.stepik.android.domain.base.analytic.AnalyticEvent

class PIPActivated : AnalyticEvent {
    override val name: String =
        "Video played in picture-in-picture"
}