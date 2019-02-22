package org.stepik.android.view.injection.video_player

import android.arch.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.video_player.VideoPlayerPresenter

@Module
abstract class VideoPlayerModule {
    @Binds
    @IntoMap
    @ViewModelKey(VideoPlayerPresenter::class)
    internal abstract fun bindVideoPlayerPresenter(videoPlayerPresenter: VideoPlayerPresenter): ViewModel
}