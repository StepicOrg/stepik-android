package org.stepik.android.view.injection.video_player

import dagger.Subcomponent
import org.stepik.android.view.video_player.ui.activity.VideoPlayerActivity

@Subcomponent(modules = [
    VideoPlayerModule::class,
    VideoTimestampDataModule::class
])
interface VideoPlayerComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): VideoPlayerComponent
    }

    fun inject(videoPlayerActivity: VideoPlayerActivity)
}