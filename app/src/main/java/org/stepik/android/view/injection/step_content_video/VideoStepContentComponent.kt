package org.stepik.android.view.injection.step_content_video

import dagger.Subcomponent
import org.stepik.android.view.step_content_video.ui.fragment.VideoStepContentFragment

@Subcomponent
interface VideoStepContentComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): VideoStepContentComponent
    }

    fun inject(videoStepContentFragment: VideoStepContentFragment)
}