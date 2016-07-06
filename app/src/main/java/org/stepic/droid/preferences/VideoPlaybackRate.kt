package org.stepic.droid.preferences

import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import org.stepic.droid.R
import org.stepic.droid.base.MainApplication
import java.util.*

enum class VideoPlaybackRate(val index: Int, val rateFloat: Float, val icon: Drawable) {
    x0_5(0, 0.5f, ContextCompat.getDrawable(MainApplication.getAppContext(), R.drawable.ic_playbackrate_0_5_light)),
    x0_75(1, 0.75f, ContextCompat.getDrawable(MainApplication.getAppContext(), R.drawable.ic_playbackrate_0_75_light)),
    x1_0(2, 1f, ContextCompat.getDrawable(MainApplication.getAppContext(), R.drawable.ic_playbackrate_1_light)),
    x1_25(3, 1.25f, ContextCompat.getDrawable(MainApplication.getAppContext(), R.drawable.ic_playbackrate_1_25_light)),
    x1_5(4, 1.5f, ContextCompat.getDrawable(MainApplication.getAppContext(), R.drawable.ic_playbackrate_1_5_light)),
    x1_75(5, 1.75f, ContextCompat.getDrawable(MainApplication.getAppContext(), R.drawable.ic_playbackrate_1_75_light)),
    x2(6, 2f, ContextCompat.getDrawable(MainApplication.getAppContext(), R.drawable.ic_playbackrate_2_0_light));

    fun getAllOptions(): List<VideoPlaybackRate> {
        return Arrays.asList<VideoPlaybackRate>(*VideoPlaybackRate.values())
    }
}
