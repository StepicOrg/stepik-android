package org.stepic.droid.di.video

import dagger.Subcomponent
import org.stepic.droid.ui.fragments.VideoExoFragment
import org.stepic.droid.ui.fragments.VideoFragment

@VideoScope
@Subcomponent(modules = arrayOf(VideoModule::class))
interface VideoComponent {

    @Subcomponent.Builder
    interface Builder {
        fun build(): VideoComponent
    }

    fun inject(videoFragment: VideoFragment)

    fun inject(videoFragment: VideoExoFragment)
}
