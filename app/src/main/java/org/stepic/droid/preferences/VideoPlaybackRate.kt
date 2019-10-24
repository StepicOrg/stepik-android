package org.stepic.droid.preferences

import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import org.stepic.droid.R
import org.stepic.droid.base.App

enum class VideoPlaybackRate(val index: Int, val rateFloat: Float, val icon: Drawable) {
    x0_5(0, 0.5f, ContextCompat.getDrawable(App.getAppContext(), R.drawable.ic_playbackrate_0_5_light)!!),
    x0_75(1, 0.75f, ContextCompat.getDrawable(App.getAppContext(), R.drawable.ic_playbackrate_0_75_light)!!),
    x1_0(2, 1f, ContextCompat.getDrawable(App.getAppContext(), R.drawable.ic_playbackrate_1_light)!!),
    x1_25(3, 1.25f, ContextCompat.getDrawable(App.getAppContext(), R.drawable.ic_playbackrate_1_25_light)!!),
    x1_5(4, 1.5f, ContextCompat.getDrawable(App.getAppContext(), R.drawable.ic_playbackrate_1_5_light)!!),
    x1_75(5, 1.75f, ContextCompat.getDrawable(App.getAppContext(), R.drawable.ic_playbackrate_1_75_light)!!),
    x2(6, 2f, ContextCompat.getDrawable(App.getAppContext(), R.drawable.ic_playbackrate_2_0_light)!!); //1.82f should be for 1080p quality for preventing freezes https://github.com/google/ExoPlayer/issues/2777

    companion object {
        fun getValueById(itemId: Int): VideoPlaybackRate {
            return when (itemId) {
                R.id.x0_5 -> {
                    VideoPlaybackRate.x0_5
                }
                R.id.x0_75 -> {
                    VideoPlaybackRate.x0_75
                }
                R.id.x1 -> {
                    VideoPlaybackRate.x1_0
                }
                R.id.x1_25 -> {
                    VideoPlaybackRate.x1_25
                }
                R.id.x1_5 -> {
                    VideoPlaybackRate.x1_5
                }
                R.id.x1_75 -> {
                    VideoPlaybackRate.x1_75
                }
                R.id.x2 -> {
                    VideoPlaybackRate.x2
                }
                else -> {
                    throw IllegalArgumentException("itemId was wrong for resolving VideoPlaybackRate")
                }
            }
        }
    }
}
