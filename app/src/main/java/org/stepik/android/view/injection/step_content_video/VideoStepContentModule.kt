package org.stepik.android.view.injection.step_content_video

import android.arch.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.step_content_video.VideoStepContentPresenter

@Module
abstract class VideoStepContentModule {
    /**
     * Presentation
     */
    @Binds
    @IntoMap
    @ViewModelKey(VideoStepContentPresenter::class)
    internal abstract fun bindVideoStepContentPresenter(videoStepContentPresenter: VideoStepContentPresenter): ViewModel
}